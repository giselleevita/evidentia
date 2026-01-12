# Azure Deployment Guide

## Prerequisites

- Azure subscription
- Azure CLI installed and configured
- Terraform installed
- kubectl configured for AKS

## Infrastructure Setup

### 1. Provision Infrastructure with Terraform

```bash
cd infra/terraform
terraform init
terraform plan -var="environment=dev" -var="db_password=<secure-password>"
terraform apply
```

This creates:
- Resource Group
- PostgreSQL Flexible Servers (evidence and audit)
- Azure Key Vault
- Network resources

### 2. Configure Azure Container Registry

```bash
az acr create --resource-group evidentia-dev --name evidentiaacr --sku Basic
az acr login --name evidentiaacr
```

### 3. Build and Push Docker Images

```bash
# Build images
docker build -f infra/docker/Dockerfile.evidence-service -t evidentiaacr.azurecr.io/evidence-service:latest .
docker build -f infra/docker/Dockerfile.audit-log-service -t evidentiaacr.azurecr.io/audit-log-service:latest .
docker build -f infra/docker/Dockerfile.integration-service -t evidentiaacr.azurecr.io/integration-service:latest .
docker build -f infra/docker/Dockerfile.frontend -t evidentiaacr.azurecr.io/frontend:latest .

# Push to ACR
docker push evidentiaacr.azurecr.io/evidence-service:latest
docker push evidentiaacr.azurecr.io/audit-log-service:latest
docker push evidentiaacr.azurecr.io/integration-service:latest
docker push evidentiaacr.azurecr.io/frontend:latest
```

### 4. Deploy to AKS

```bash
# Create namespace
kubectl create namespace evidentia

# Update image references in k8s manifests
# Replace <ACR_NAME> with your ACR name

# Apply deployments
kubectl apply -f infra/k8s/evidence-service-deployment.yaml
kubectl apply -f infra/k8s/audit-log-service-deployment.yaml
```

## Configuration

### Secrets

Store secrets in Azure Key Vault:
```bash
az keyvault secret set --vault-name evidentia-kv-dev --name evidence-db-url --value "jdbc:postgresql://..."
az keyvault secret set --vault-name evidentia-kv-dev --name audit-db-url --value "jdbc:postgresql://..."
```

### ConfigMaps

Create ConfigMaps for non-sensitive configuration:
```bash
kubectl create configmap azure-config --from-literal=issuer-uri="https://login.microsoftonline.com/..." -n evidentia
```

## Monitoring

- Azure Application Insights for application monitoring
- Azure Monitor for infrastructure metrics
- Log Analytics for centralized logging
