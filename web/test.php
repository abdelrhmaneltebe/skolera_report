<html>
   <body>
      <form action = "http://report.skolera.com:8080" method = "POST" target = "_blank">
	  <TABLE cellpadding="5" cellspacing="3" style="margin-bottom:30px">
		<TR><TD>* $P_DB_NAME: 					</TD><TD><input  name="$P_DB_NAME" value="sgis" /> </TD></TR>
		<TR><TD>* $P_REPORT_NAME: 				</TD><TD><select name="$P_REPORT_NAME" value="NO"> <option value ="QUARTER" selected="selected">QUARTER</option><option value="SEMESTER" >SEMESTER</option><option value="YEAR" >YEAR</option></select> </TD></TR>
		<TR><TD>  $P_LETTER_ID:   				</TD><TD><input  name="$P_LETTER_ID" value="790" /> </TD></TR>
		<TR><TD>  $P_GPA_ID:   				</TD><TD><input  name="$P_GPA_ID" value="" /> </TD></TR>

		<TR><TD>  $P_GRADING_PERIOD_ID: 		</TD><TD><input  name="$P_GRADING_PERIOD_ID" value='"1483","1484"' />  </TD></TR>
		<TR><TD>  $P_LEVEL_CODE:				</TD><TD><input  name="$P_LEVEL_CODE" value='"G05","G06","G07","G08"' />  </TD></TR>
		<TR><TD>  $P_STUDENT_ID:				</TD><TD><input  name="$P_STUDENT_ID" value='"570","581"' />  </TD></TR>
		<TR><TD>  $P_CATEGORY_NAME:				</TD><TD><input  name="$P_CATEGORY_NAME" value='"Assignments","Final Exam","Project"' style="width:300px"/>  </TD></TR>
		<TR><TD>  $P_CLASS_NAME: 				</TD><TD><input  name="$P_CLASS_NAME" value='"19B","19A"' /> </TD></TR>
		<TR><TD>  $P_INCLUDE_ATTENDANCE_RECORD:	</TD><TD><select name="$P_INCLUDE_ATTENDANCE_RECORD"  value="YES"> <option value ="YES">YES</option><option value="NO">NO</option></select> </TD></TR>
		<TR><TD>  $P_INCLUDE_COMMENT_SECTION:	</TD><TD><select name="$P_INCLUDE_COMMENT_SECTION" value="YES"> <option value ="YES">YES</option><option value="NO">NO</option></select> </TD></TR>
		<TR><TD>  $P_SEND_BY_EMAIL:				</TD><TD><select name="$P_SEND_BY_EMAIL" value="NO"> <option value ="YES">YES</option><option value="NO" selected="selected">NO</option></select> </TD></TR>
		<TR><TD>  $P_EMAIL_LIST:				</TD><TD><input  name="$P_EMAIL_LIST" value='"kelbedweihy@gmail.com","hani.shafei@gmail.com"' style="width:400px"/> </TD></TR>
		<TR><TD>  $P_SHOW_ERRORS:				</TD><TD><select name="$P_SHOW_ERRORS" value="YES"> <option value ="YES">YES</option><option value="NO">NO</option></select> </TD></TR>
	</TABLE>
        
         <input type="submit" value="View Report" />
      </form>
   </body>
</html>