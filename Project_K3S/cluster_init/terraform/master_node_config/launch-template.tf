resource "aws_launch_template" "k3s_master" {
  name_prefix   = "k3s-master-"
  image_id      = "ami-053b0d53c279acc90" # Update with the correct AMI ID
  instance_type = "c7a.medium"             # Update as necessary
  key_name      = "jenkins-ansible"       # Update with your SSH key name
  # default_version = 1 suppose to be used to set launch tempolate to latest version


  vpc_security_group_ids = [data.aws_security_group.k3s_sg.id]
  iam_instance_profile {
    name = data.aws_iam_instance_profile.k3s_node_instance_profile.name
  }

  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = "master"
    }
  }
}
