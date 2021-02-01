###############
# RETROGRADLE #
#  BY CURLE   #
#     2020    #
###############

# This file allows you to convert MCP patches to MCPConfig patches.
# This does not expand the name of the file to the folder structure.

# Part one of eight in the RetroGradle series.

import os

def getListOfFiles(dirName):
    # create a list of file and sub directories 
    # names in the given directory 
    listOfFile = os.listdir(dirName)
    allFiles = list()
    # Iterate over all the entries
    for entry in listOfFile:
        # Create full path
        fullPath = os.path.join(dirName, entry)
        # If entry is a directory then get the list of files in this directory 
        if os.path.isdir(fullPath):
            allFiles = allFiles + getListOfFiles(fullPath)
        else:
            allFiles.append(fullPath)
                
    return allFiles        
 

cwd = os.getcwd()

files = getListOfFiles(cwd)


for file in files:
    lines = []
    with open(file, "r+") as wfile:
        if file[-1] == 'h': # dirty dirty hack to make sure we're only modifying .java.patc>h< files
            lines = wfile.readlines()
            if lines[0].startswith('diff'):
                print(lines.pop(0) + " removed.")

            # Lines 1 and 2 use the form:
            #   --- a\net\minecraft\command\CommandSpreadPlayers.java
            #   +++ b\net\minecraft\command\CommandSpreadPlayers.java
            # We need the separators to be / , for cross-compatibility.

            lines[0] = lines[0].replace("\\", '/')
            lines[1] = lines[1].replace("\\", '/')

            # MCP Patches use the form:
            #   --- minecraft\net\minecraft\command\CommandSpreadPlayers.java
            #   +++ minecraft_patched\net\minecraft\command\CommandSpreadPlayers.java
            # We need to replace the minecraft, minecraft_patched
            # with the format a, b seen above.
            #
            # Unfortunately, it's not as simple as just swapping out the text,
            # as there is also:
            #   --- minecraft_server\net\minecraft\command\CommandSpreadPlayers.java
            #   +++ minecraft_server_patched\net\minecraft\command\CommandSpreadPlayers.java
            # So, we need to look for "minecraft/" (so that minecraft_patched doesn't match)
            #   etc etc.


            if "minecraft_server/" in lines[0]:
                print("Replacing server headers..")
                # if the first line contains "minecraft_server" then
                # the second line must contain "minecraft_server_patched".

                lines[0] = lines[0].replace("minecraft_server/", "a/", 1)
                lines[1] = lines[1].replace("minecraft_server_patched/", "b/", 1)

                # These two checks must be mutually exclusive, else
                # minecraft_server/net/>minecraft/< will match
            elif "minecraft/" in lines[0]:
                print("Replacing client/joined headers..")
                # likewise, if the first line contains "minecraft/" then the second line must contain
                # "minecraft_patched", so we take care of them both here.

                lines[0] = lines[0].replace("minecraft/", "a/", 1)
                lines[1] = lines[1].replace("minecraft_patched/", "b/", 1)

            
    with open(file, "w") as wfile:
        wfile.write(''.join(lines))    
    
