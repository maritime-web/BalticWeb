#!/bin/bash

echo "Stopping containers"
docker stop balticweb db couch watchtower

full ()
{
    echo "Removing containers"
    docker rm balticweb db couch watchtower
    docker network rm baltic
}

$1

exit 0
