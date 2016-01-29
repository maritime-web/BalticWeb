# If modifying this script then save a copy in Embryo/embryo-web/src/test/scripts/installJsUnitTestOnJenkins.sh

# This script should be added as a Jenkins pre build step to the Maven build job, which is executing karma

# install nodejs, if using cloudbees (and if not already installed)
curl -s -o use-node https://repository-cloudbees.forge.cloudbees.com/distributions/ci-addons/node/use-node
NODE_VERSION=0.11.2 source ./use-node

ARCH=`uname -m`
node_name=node-${NODE_VERSION}-${ARCH}


# install phantomjs, karma
[ -d /scratch/jenkins/addons/node/$node_name/lib/node_modules/grunt-cli ] || npm install -g grunt-cli


[ -d $HOME/bin ] || mkdir $HOME/bin
[ -f $HOME/bin/node ] || ln -sf /scratch/jenkins/addons/node/$node_name/bin/node $HOME/bin/node
[ -f $HOME/bin/npm ] || ln -sf /scratch/jenkins/addons/node/$node_name/bin/npm $HOME/bin/npm
[ -f $HOME/bin/grunt ] || ln -sf /scratch/jenkins/addons/node/$node_name/bin/grunt $HOME/bin/grunt
