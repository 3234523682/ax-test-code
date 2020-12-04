@echo off

::最小文件大小为：10MB
SET minFileSize=10485760
::设置bat所在目录
SET "currentPath=%~dp0\11111111"
::获取文件数量
set "fileNum=100"
if exist "%currentPath%" 
if not exist "%currentPath%" md "%currentPath%"
>"%tmp%\t.js" echo;WSH.echo(WSH.StdIn.ReadAll().split(/[\r\n]+/).sort(function(){return Math.random()^>.5 ? -1 : 1;}).slice(0,%fileNum%).join('\r\n'))
cd /d "%~dp0"
for /f "delims=" %%a in ('dir /a-d/b/s *.jpg^|cscript -nologo -e:jscript "%tmp%\t.js"') do (
	::创建文件快捷方式
	MKLINK %currentPath%\%%~nxa "%%a"
)
pause