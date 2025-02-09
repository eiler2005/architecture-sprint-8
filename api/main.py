import requests
import time
import base64
import json
from fastapi import FastAPI, Depends, HTTPException
from fastapi.security import OAuth2AuthorizationCodeBearer
from fastapi.middleware.cors import CORSMiddleware
from jose import JWTError, jwt
from typing import List
import os
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.hazmat.primitives import serialization

# Настройки Keycloak
KEYCLOAK_URL = os.getenv("KEYCLOAK_URL", "http://keycloak:8080/realms/reports-realm")
CLIENT_ID = os.getenv("CLIENT_ID", "reports-api")
ALGORITHM = os.getenv("ALGORITHM", "RS256")

def get_public_key():
    url = f"{KEYCLOAK_URL}/protocol/openid-connect/certs"
    max_retries = 5
    for attempt in range(max_retries):
        try:
            print(f"Fetching Keycloak public key (attempt {attempt+1})...")
            response = requests.get(url, timeout=5)
            response.raise_for_status()
            jwks = response.json()
            keys = jwks.get("keys", [])

            if not keys:
                print("No keys found in JWKS, retrying...")
                time.sleep(2)
                continue

            # Находим ключ для подписи (use: "sig")
            signing_keys = [key for key in keys if key.get("use") == "sig" and key.get("alg") == "RS256"]

            if not signing_keys:
                raise ValueError("No signing keys found in JWKS")

            key = signing_keys[0]  # Берем первый ключ для подписи

            # 1. Если есть `x5c`, используем его
            if "x5c" in key:
                print("Using x5c certificate for public key")
                cert_str = f"-----BEGIN CERTIFICATE-----\n{key['x5c'][0]}\n-----END CERTIFICATE-----"
                return cert_str

            # 2. Если `x5c` нет, конвертируем `n` и `e` в PEM
            print("Warning: x5c not found, converting n/e to PEM format")
            n_b64 = key["n"]
            e_b64 = key["e"]
            n = int.from_bytes(base64.urlsafe_b64decode(n_b64 + "=="), "big")
            e = int.from_bytes(base64.urlsafe_b64decode(e_b64 + "=="), "big")

            public_key = rsa.RSAPublicNumbers(e, n).public_key()
            pem = public_key.public_bytes(
                encoding=serialization.Encoding.PEM,
                format=serialization.PublicFormat.SubjectPublicKeyInfo
            ).decode("utf-8")

            return pem
        except Exception as e:
            print(f"Error fetching Keycloak public key: {e}")
            time.sleep(2)

    raise RuntimeError("Failed to fetch Keycloak public key after multiple attempts.")

# Получаем ключ перед запуском
PUBLIC_KEY = get_public_key()

oauth2_scheme = OAuth2AuthorizationCodeBearer(
    tokenUrl=f"{KEYCLOAK_URL}/protocol/openid-connect/token",
    authorizationUrl=f"{KEYCLOAK_URL}/protocol/openid-connect/auth"
)

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

def verify_token(token: str = Depends(oauth2_scheme)):
    try:
        payload = jwt.decode(token, PUBLIC_KEY, algorithms=[ALGORITHM], audience=CLIENT_ID)
        roles = payload.get("realm_access", {}).get("roles", [])

        if "prothetic_user" not in roles:
            raise HTTPException(status_code=403, detail="Access denied")

        return payload
    except JWTError as e:
        print(f"JWT decode error: {e}")
        raise HTTPException(status_code=401, detail="Invalid token")

@app.get("/reports", response_model=List[dict])
def get_reports(user: dict = Depends(verify_token)):
    return [{
        "user_id": user["sub"],
        "report": "Sample report data for prosthetic user."
    }]

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host=os.getenv("API_HOST", "0.0.0.0"), port=int(os.getenv("API_PORT", 8000)))