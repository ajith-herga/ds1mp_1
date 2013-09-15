dsmp1
=====

Code for Distributed Systems: MP1

Prepare:
make clean
make

Run Server:
java GrepServer
Expects log file /tmp/machine.log_45

Query from Client:
java GrepClient <keyregex> <valueregex>

Test from Client:
java GrepClient __test grep <keyregex> <valueregex>
