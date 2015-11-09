This document for internal use.

# Setting Mime-types in Windows #

First, use Tortoise SVN to checkout the javadoc directory from SVN.  Then, copy the javadoc folder of the tag into the root of the javadoc directory, such that the javadoc for tag 'struts2-conversation-x.x' is in the the location 'javadoc/struts2-conversation-x.x'.  Then use Tortoise SVN's 'add' command to add the folder to version control.  DO NOT COMMIT THE FOLEDER YET!

Then, using [SlikSvn](http://www.sliksvn.com/en/download):

```
C:\PATH_TO_JAVADOCS>for /r %1 in (*.gif) do svn propset svn:mime-type image/gif "%~f1"

C:\PATH_TO_JAVADOCS>for /r %1 in (*.css) do svn propset svn:mime-type text/css "%~f1"


C:\PATH_TO_JAVADOCS>for /r %1 in (*.png) do svn propset svn:mime-type image/png "%~f1"

C:\PATH_TO_JAVADOCS>for /r %1 in (*.htm*) do svn propset svn:mime-type text/html "%~f1"
```

Then, commit the folder.

# Links to Javadoc #
If the link is still showing the source instead of the page, expand "Show Details" (if not already expanded) and click on "View raw file" on the right side of the page.