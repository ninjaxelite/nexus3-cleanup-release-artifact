# nexus3-cleanup-release-artifact
Nexus Repository Manager 3 supports groovy scripts which can be uploaded and run in the Nexus Manager.

This groovy script will clean up your nexus repository by deleting outdated components.

You can define the

- retentionDays
- retentionCount
- repositoryName
- whitelist 

at the beginning of the script and then run it to clean up your nexus repository.
