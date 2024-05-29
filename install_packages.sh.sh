#!/bin/bash

# Function to install Homebrew if it's not already installed
install_homebrew() {
    if ! command -v brew &> /dev/null; then
        echo "Homebrew not found. Installing Homebrew..."
        /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
        echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zprofile
        eval "$(/opt/homebrew/bin/brew shellenv)"
    else
        echo "Homebrew is already installed."
    fi
}

# Function to install or upgrade Java
install_java() {
    echo "Installing/Upgrading Java 17.0.8..."
    brew tap AdoptOpenJDK/openjdk
    brew install --cask adoptopenjdk17
    echo "Java version:"
    java -version
}

# Function to install or upgrade Git
install_git() {
    echo "Installing/Upgrading Git 2.45.0..."
    brew install git
    brew link --overwrite git
    echo "Git version:"
    git --version
}

# Function to install or upgrade Maven
install_maven() {
    echo "Installing/Upgrading Maven 3.9.1..."
    brew install maven
    echo "Maven version:"
    mvn -version
}

# Main script execution
install_homebrew
brew update
install_java
install_git
install_maven

echo "Installation/Upgrade complete."

