###############
# RETROGRADLE #
#  BY CURLE   #
#     2020    #
###############

# This file allows you to split an MCP joined.exc file up to the MCPConfig files 
# constructors.txt, access.txt and exceptions.txt.

# Part seven of eight in the RetroGradle series.

import re

ctrRegStr = "(\.<init>)(\(\S*)(=\S*\|p_i)(\d*)"
exceptionRegStr = "(\(\S*\)\S*)=(\S*)\|"
accessRegStr = "(\S*)\.(\S*)(\(\S*\)\S*)-Access=(\S*)"

def main():
	print("Converting exc to MCPConfig Data:")
	ctrReg = re.compile(ctrRegStr)
	exceptionReg = re.compile(exceptionRegStr)
	accessReg = re.compile(accessRegStr)
	
	try:
		excFile = open("joined.exc", "r")
		constructorsFile = open("constructors.txt", "w")
		exceptionsFile = open("exceptions.txt", "w")
		accessFile = open("access.txt", "w")

		excLines = excFile.readlines()

		print("There are " + str(len(excLines)) + " lines of exc")
		lineNo = 0
		constructorLines = []
		exceptionsLines = []
		accessLines = []
		for excLine in excLines:
			lineType = 0
			lineNo += 1
			constructorLine = []
			exceptionsLine = []
			accessLine = []
			constructorMatches = re.search(ctrReg, excLine)
			exceptionsMatches = re.search(exceptionReg, excLine)
			accessMatches = re.search(accessReg, excLine)

			# If we match ".<init>" we're a constructor.
			if isinstance(constructorMatches, re.Match):
				# constructors.txt lines are in the form
				# SRG CLASS SIGNATURE

				# Matches are in the order
				# .<init> SIGNATURE =|p_i SRG
				# CLASS is line[0]..match0

				constructorLine.append(constructorMatches[4])
				className = excLine[0:constructorMatches.start(0)]
				#print("Processing constructor of class " + className)
				constructorLine.append(className)
				constructorLine.append(constructorMatches[2])

				constructorLineString = " ".join(constructorLine)

				constructorLines.append(constructorLineString)

				lineType = 1

			# if we match (SOMETHING)SOMETHING=SOMETHING|, we're an exception line
			if isinstance(exceptionsMatches, re.Match):
				# exceptions.txt is in the form 
				# CLASS/FUNCTION (PARAMS)RETURN EXCEPTION

				# Matches are in the order
				# (PARAMS)RETURN = EXCEPTION | 
				matches = exceptionsMatches.groups()
				className = excLine[0:exceptionsMatches.start(0)].replace(".", "/")

				#print("Processing exception of class " + className + ": " + str(matches))
				exceptionsLine.append(className)
				exceptionsLine.append(matches[0])
				exceptionsLine.append(matches[1])

				exceptionsLineStr = " ".join(exceptionsLine)

				exceptionsLines.append(exceptionsLineStr)

				lineType = 2

			if isinstance(accessMatches, re.Match):
				# access.txt is in the form
				# ACCESS CLASS OBJECT SIGNATURE

				# accessMatches is in the form
				# CLASS OBJECT SIGNATURE ACCESS
				matches = accessMatches.groups()

				print("Handling access modifier for " + matches[1] + " to " + matches[3])
				accessLine.append(matches[3])
				accessLine.append(matches[0])
				accessLine.append(matches[1])
				accessLine.append(matches[2])

				accessLineStr = " ".join(accessLine)

				accessLines.append(accessLineStr)

				lineType = 3

			if lineType == 0:
				print("No useful data on line " + str(lineNo))
				continue
		
		
		constructorLines.sort()
		constructorsFile.write("\n".join(constructorLines))

		exceptionsLines.sort()
		exceptionsFile.write("\n".join(exceptionsLines))

		accessLines.sort()
		accessFile.write("\n".join(accessLines))

	finally:

		excFile.close()
		constructorsFile.close()
		accessFile.close()

main()