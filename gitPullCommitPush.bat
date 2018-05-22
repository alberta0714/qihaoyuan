git pull
git add *
@set /p comment="请输入本次提交的备注:"
git commit -a -m "备注:%comment%"
git push & pause

