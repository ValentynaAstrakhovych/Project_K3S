# Create an Internet Gateway
resource "aws_internet_gateway" "k3s_igw" {
  vpc_id = aws_vpc.k3s_vpc.id

  tags = {
    Name = "K3s_IGW"
  }
}

# Allocate an Elastic IP for the NAT Gateway
resource "aws_eip" "k3s_nat_eip" {
  vpc = true
}

# Create a NAT Gateway
resource "aws_nat_gateway" "k3s_nat_gateway" {
  allocation_id = aws_eip.k3s_nat_eip.id
  subnet_id     = aws_subnet.k3s_public_subnet[0].id
  tags = {
    Name = "K3s_NAT_Gateway"
  }
}