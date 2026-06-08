#docker build -f ./Dockerfile -t docker-spring  .
#docker build -t docker-spring  . --no-cache is also helpful when you want to make sure that you are building the image from
# scratch without using any cached layers. This can be useful if you have made changes to the Dockerfile or
# if you want to ensure that you are using the latest versions of the base images and dependencies.
docker build -t docker-spring  . --no-cache
docker-compose up -d