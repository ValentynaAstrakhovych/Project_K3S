#!/bin/bash

# Check if the correct number of arguments was passed
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <path_to_ip_file> <path_to_kubeconfig>"
    exit 1
fi

# Path to the file containing the IP addresses
IP_FILE="$1"

# Path to the kubeconfig file
KUBECONFIG_FILE="$2"

# Read the first IP address from the file
new_ip=$(cat "$IP_FILE" | tr -d '[:space:]' | awk -F',' '{print $1}')

# Check that the new IP address is not empty
if [ -z "$new_ip" ]; then
  echo "Error: IP address not found."
  exit 1
fi

# Replace the IP address in the kubeconfig file
# Notice the removal of '' after -i, which is specific to Linux
sed -i "s/https:\/\/127.0.0.1:6443/https:\/\/$new_ip:6443/g" "$KUBECONFIG_FILE"

echo "The IP address in the kubeconfig file has been successfully updated to $new_ip"
