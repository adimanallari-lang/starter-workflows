#!/usr/bin/env bash
set -e
echo "Building FurniturePlugin (requires internet access to repo.papermc.io + repo.opencollab.dev)..."
mvn -f "$(dirname "$0")/pom.xml" clean package
echo
echo "Done. Jar at: target/FurniturePlugin.jar"
