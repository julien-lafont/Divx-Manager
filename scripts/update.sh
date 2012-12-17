./stop.sh
git stash
git pull --rebase
git stash apply
./start.sh
