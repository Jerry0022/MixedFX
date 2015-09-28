; Waits until LogMeIn Hamachi GUI was opened, put it to front and if it is offline it starts it

WinWait, LogMeIn Hamachi
Loop 
{
	ControlClick, Button1, LogMeIn Hamachi
	; Wait until Button1 was found (full hamachi gui was started)
	if(ErrorLevel = 0)
		break
}
ControlGetText, text, Static2, LogMeIn Hamachi
WinActivate, LogMeIn Hamachi
if (text = "Offline")
	ControlClick, Button1, LogMeIn Hamachi