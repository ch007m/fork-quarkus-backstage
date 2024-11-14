#!/bin/sh
#-------------------------------------
# Hook invoked by the release workflow
#-------------------------------------

# Update the package list
sudo apt-get update

# Install Asciicinema
sudo apt-get install asciinema

# Install Asciinema-automation
python3 -m pip install asciinema-automation

# Install Asciinema-Agg
cargo install --git https://github.com/asciinema/agg

# Install Quarkus CLI
QUARKUS_VERSION="3.16.3" # Replace with the desired version
QUARKUS_CLI_URL="https://repo1.maven.org/maven2/io/quarkus/quarkus-cli/${QUARKUS_VERSION}/quarkus-cli-${QUARKUS_VERSION}-runner.jar"
echo "Downloading Quarkus CLI from ${QUARKUS_CLI_URL}"
mkdir ~/tools
curl -Ls ${QUARKUS_CLI_URL} -o ~/tools/quarkus-cli.jar
mkdir ~/.local/bin -p
echo "#!/bin/sh" > ~/.local/bin/quarkus
echo "java -jar ${HOME}/tools/quarkus-cli.jar \$*" >> ~/.local/bin/quarkus
chmod +x ~/.local/bin/quarkus
quarkus version

# Install JBang
JBANG_VERSION="0.119.0"
JBANG_CLI_URL="https://repo1.maven.org/maven2/dev/jbang/jbang-cli/${JBANG_VERSION}/jbang-cli-${JBANG_VERSION}-all.jar"
echo "Downloading JBang CLI from ${JBANG_CLI_URL}"
mkdir ~/tools
curl -Ls ${JBANG_CLI_URL} -o ~/tools/jbang-all.jar
mkdir ~/.local/bin -p
echo "#!/bin/sh" > ~/.local/bin/jbang
echo "java -jar ${HOME}/tools/jbang-all.jar \$*" >> ~/.local/bin/jbang
chmod +x ~/.local/bin/jbang
jbang version


# Iterate all .sh files in the docs/modules/ROOT/assets/scenarios directory
# and replace 999-SNAPSHOT with the current version
sed -i "s/999-SNAPSHOT/${CURRENT_VERSION}/g" docs/modules/ROOT/assets/scenarios/*.sh

# Generate screencasts
cd docs/modules/ROOT/assets/ 
./generate-screencasts 
cd ../../../../

# Commit changes 
git add docs/modules/ROOT/assets/casts
git add docs/modules/ROOT/assets/images
git commit -m "Update screencasts for ${CURRENT_VERSION}"
