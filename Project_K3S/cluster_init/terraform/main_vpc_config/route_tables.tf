# Public Route Table
resource "aws_route_table" "k3s_public_route_table" {
  vpc_id = aws_vpc.k3s_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.k3s_igw.id
  }

  route {
    cidr_block                = data.aws_vpc.default.cidr_block
    vpc_peering_connection_id = aws_vpc_peering_connection.k3s_vpc_peering.id
  }

  tags = {
    Name = "K3s_Public_Route_Table"
  }
}

# Associate Public Route Table with each Public Subnet
resource "aws_route_table_association" "k3s_public_subnet_association" {
  count = length(aws_subnet.k3s_public_subnet.*.id)

  subnet_id      = aws_subnet.k3s_public_subnet[count.index].id
  route_table_id = aws_route_table.k3s_public_route_table.id
}

# Private Route Table
resource "aws_route_table" "k3s_private_route_table" {
  vpc_id = aws_vpc.k3s_vpc.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.k3s_nat_gateway.id
  }

  route {
    cidr_block                = data.aws_vpc.default.cidr_block
    vpc_peering_connection_id = aws_vpc_peering_connection.k3s_vpc_peering.id
  }

  tags = {
    Name = "K3s_Private_Route_Table"
  }
}

# Associate Private Route Table with each Private Subnet
resource "aws_route_table_association" "k3s_private_subnet_association" {
  count = length(aws_subnet.k3s_private_subnet.*.id)

  subnet_id      = aws_subnet.k3s_private_subnet[count.index].id
  route_table_id = aws_route_table.k3s_private_route_table.id
}

# Default Route Table for VPC Peering
resource "aws_route_table" "default_back_to_k3s_peering" {
  vpc_id = data.aws_vpc.default.id

  route {
    cidr_block                = aws_vpc.k3s_vpc.cidr_block
    vpc_peering_connection_id = aws_vpc_peering_connection.k3s_vpc_peering.id
  }

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = data.aws_internet_gateway.default.id
  }

  tags = {
    Name = "default_back_to_k3s_peering"
  }
}

# Association for Default Route Table with VPC Peering
resource "aws_route_table_association" "default_back_to_k3s_peering_association" {
  for_each       = toset(data.aws_subnets.default.ids)
  subnet_id      = each.value
  route_table_id = aws_route_table.default_back_to_k3s_peering.id
}
