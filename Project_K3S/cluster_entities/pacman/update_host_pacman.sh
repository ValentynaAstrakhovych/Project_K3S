#!/bin/bash

# File path
file_path="packman-deployment.yaml"
new_host=$k3s_master_instance_public_dns

# Step 1: Replace the host and extract the Ingress section
sed -i "/^    - host: /s/.*/    - host: $new_host/" "$file_path"

# Step 2: Extract the Ingress section
ingress_section=$(awk '/^# Ingress Definition/,/^---$/' "$file_path")

# Step 3: Remove the old Ingress section from the file
sed -i '/^# Ingress Definition/,/^---$/d' "$file_path"

# Step 4: Prepend the new Ingress section to the file
echo "$ingress_section" | cat - "$file_path" > temp && mv temp "$file_path"

echo "Ingress updated and moved to the top of the file."
