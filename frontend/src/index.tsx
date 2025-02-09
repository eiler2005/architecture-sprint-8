import React from "react";
import ReactDOM from "react-dom";
import { ReactKeycloakProvider } from "@react-keycloak/web";
import Keycloak from "keycloak-js";
import App from "./App";

const keycloak = new Keycloak({
    url: 'http://localhost:808docker-compose down\n0',  // Убери `/auth`
    realm: 'reports-realm',
    clientId: 'reports-frontend'
});

ReactDOM.render(
    <ReactKeycloakProvider
        authClient={keycloak}
        initOptions={{
            onLoad: "login-required",
            // можно и другие настройки init передать, например:
            // checkLoginIframe: false,
            pkceMethod: 'S256',
        }}
    >
        <App />
    </ReactKeycloakProvider>,
    document.getElementById("root")
);