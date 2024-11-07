# Define the VPC
resource "aws_vpc" "k3s_vpc" {
  cidr_block           = "10.0.0.0/16" # 255.255.0.0 = 65,536 IPs
  enable_dns_hostnames = true

  tags = {
    Name = "K3s_VPC"
  }
}

# Create a VPC Peering Connection
resource "aws_vpc_peering_connection" "k3s_vpc_peering" {
  peer_vpc_id = data.aws_vpc.default.id // This is the default VPC ID
  vpc_id      = aws_vpc.k3s_vpc.id // This is the VPC ID we created
  auto_accept = true

  tags = {
    Name = "K3s_VPC_Peering"
  }
}
