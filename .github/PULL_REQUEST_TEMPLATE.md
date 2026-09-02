## Summary

Describe the behavior changed and why.

## Verification

- [ ] `./gradlew build --no-daemon`
- [ ] `cd frontend/compliance-portal && npm ci && npm run lint && npm test -- --run && npm run build`
- [ ] Security and tenant-isolation implications reviewed
- [ ] Documentation updated where behavior or limitations changed
