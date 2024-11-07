
# Create Public Subnets in all AZs
resource "aws_subnet" "k3s_public_subnet" {
  count                   = length(data.aws_availability_zones.available.names) # example [az1, az2, az3]
  vpc_id                  = aws_vpc.k3s_vpc.id
  cidr_block              = cidrsubnet(var.public_subnet_cidr, 4, count.index)
  map_public_ip_on_launch = true
  availability_zone       = data.aws_availability_zones.available.names[count.index]

  tags = {
    Name = format("K3s_Public_Subnet_%s", data.aws_availability_zones.available.names[count.index])
  }
}

# Create Private Subnets in all AZs
resource "aws_subnet" "k3s_private_subnet" {
  count = length(data.aws_availability_zones.available.names)

  vpc_id            = aws_vpc.k3s_vpc.id
  cidr_block        = cidrsubnet(var.private_subnet_cidr, 4, count.index)
  availability_zone = data.aws_availability_zones.available.names[count.index]

  tags = {
    Name = format("K3s_Private_Subnet_%s", data.aws_availability_zones.available.names[count.index])
  }
}