# Updates

## [1.21.9 Support and Rendering Refactor]

- **Minecraft 1.21.9 Support**: Updated the mod to be compatible with Minecraft 1.21.9.
- **Rendering System Overhaul**: Refactored the rendering engine to support the new `AvatarRenderer` and `AvatarRenderState` system introduced in Minecraft 1.21.2+.
- **Improved Geometry Handling**: Fixed issues with Bedrock geometry transformations not being applied correctly in the new rendering pipeline.
- **Internal Refactoring**: Updated player skin handling to use the new `ClientAsset` system.
- **Bug Fixes**:
  - Fixed a case-sensitivity issue with the mod icon path in `fabric.mod.json`.
- **Dependency Updates**:
  - Updated Fabric Loader requirement to 0.17.2 or higher.
  - Updated Fabric API version.
