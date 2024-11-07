# Variable definitions for CIDR blocks
variable "public_subnet_cidr" {
  description = "The CIDR block for the VPC"
  default     = "10.0.1.0/24" # 255.255.255.0 range 255 IPs
}

variable "private_subnet_cidr" {
  description = "The CIDR block for the VPC"
  default     = "10.0.2.0/24" # 255.255.255.0 range 255 IPs
}