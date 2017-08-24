clear
git st
comment="auto save "
comment=+$(date)
git commit -am "$comment"
git push origin master
git st

