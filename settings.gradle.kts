rootProject.name = "evidentia"

include(
    "backend:common",
    "backend:evidence-service",
    "backend:audit-log-service",
    "backend:incident-service",
    "backend:integration-service",
    "backend:rating-service"
)
