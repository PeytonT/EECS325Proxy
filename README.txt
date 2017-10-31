Peyton Turner
EECS 325
Project 1 README

(a) Information which proxy port you are using. 

Alphabetically I am student #45 and should be on port 5045, but in accordance with the instructions that the proxy will be run using the command “java proxyd –port 50025” for student #25 I have arranged for the proxy to take arguments and use the second argument as the port number.


(b) Instructions to operate the proxy.

Go to the /src folder.
run the command “javac proxyd.java”
run the command “java proxyd –port port_number”
Configure your browser to connect to the proxy on the IP of the machine you ran the proxy on at the port you specified. 


(c) Which browser you used to test it with.

I have tested with Chrome, Safari, and Firefox.

 
(d) Which web sites you tested on.

case.edu
case.edu/about
case.edu/admissions
case.edu/athletics
case.edu/campuslife
case.edu/research
case.edu/schools
cluster41.case.edu/
cluster41.case.edu/action.php


(e) Any notes on residual bugs or weird behaviors you were unable to chase down.

Firefox either doesn’t display or corrupts large images. However, I can reproduce these problems when not using my proxy, so I think Firefox rather than my proxy is to blame.

I occasionally get an exception that arises as a consequence of the HTTP Header/Body delimiter not being found anywhere in the message. I assume that messages are being sent in some format that I’m unfamiliar with.