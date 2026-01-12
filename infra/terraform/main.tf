terraform {
  required_version = ">= 1.0"
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0"
    }
  }
}

provider "azurerm" {
  features {}
}

variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string
  default     = "dev"
}

variable "location" {
  description = "Azure region"
  type        = string
  default     = "eastus"
}

resource "azurerm_resource_group" "main" {
  name     = "evidentia-${var.environment}"
  location = var.location
}

resource "azurerm_postgresql_flexible_server" "evidence" {
  name                   = "evidentia-evidence-${var.environment}"
  resource_group_name    = azurerm_resource_group.main.name
  location               = var.location
  version                = "16"
  administrator_login    = "evidentia"
  administrator_password = var.db_password

  sku_name = "B_Standard_B1ms"

  depends_on = [azurerm_resource_group.main]
}

resource "azurerm_postgresql_flexible_server" "audit" {
  name                   = "evidentia-audit-${var.environment}"
  resource_group_name    = azurerm_resource_group.main.name
  location               = var.location
  version                = "16"
  administrator_login    = "evidentia"
  administrator_password = var.db_password

  sku_name = "B_Standard_B1ms"

  depends_on = [azurerm_resource_group.main]
}

resource "azurerm_key_vault" "main" {
  name                = "evidentia-kv-${var.environment}"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  tenant_id           = data.azurerm_client_config.current.tenant_id
  sku_name            = "standard"
}

data "azurerm_client_config" "current" {}

variable "db_password" {
  description = "Database password"
  type        = string
  sensitive   = true
}
