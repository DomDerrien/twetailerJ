#! /usr/bin/env python
# -*- coding: UTF-8 -*-

import os, sys, shutil, getopt

#
# Global variables
#
enRoot = "CarDealers"
frRoot = "Automobiles"

enTopQualifier = "Pre-Owned"
enSubQualifier = "Used"
frTopQualifier = "d'Occasion"
frSubQualifier = "Usagee"
frNiceQualifier = "Usagée"

enDefaultCity = "Local"
frDefaultCity = "votre Localité"

enDefaultMake = "Cars"
frDefaultMake = "Automobile"

#
# Print the program usage
#
def usage():
    print("-c, --city          City label")
    print("-p, --postalCode    Postal code, should be surrounded by a double-quotes pair if it contains a space character")
    print("-b, --baseFilename  Base of the template filename, that should match files <base>_en.html and <base>_fr.html. Default value: 'index'")
    print("-o, --output        Folder where the templates will be propagated")
    print("-s, --silent        To bypass the prompt asking for a confirmation of the propagation")
    print("")

#
# Extract the program parameters
#
def extractParameters(arguments):
    extractedData = ["", "", "index", ".", False]
    try:
        optionList, optionValues = getopt.getopt(arguments, "b:c:p:o:s", ["baseFilename=", "city=", "postalCode=", "output=", "silent"])
    except getopt.GetoptError:
        usage()
        sys.exit(2)
    for option, value in optionList:
        if option in ["-c", "--city"]:
            extractedData[0] = value
        elif option in ["-p", "--postalCode"]:
            extractedData[1] = value
        elif option in ["-b", "--baseFilename"]:
            extractedData[2] = value
        elif option in ["-o", "--output"]:
            extractedData[3] = value
        elif option in ["-s", "--silent"]:
            extractedData[4] = value
    if extractedData[0] == "" or extractedData[1] == "":
        usage()
        sys.exit(2)
    if os.path.exists(extractedData[3]) != True:
        print("Destination path '" + extractedData[3] + "' does not exist!")
        print("")
        usage()
        sys.exit()
    extractedData[3] = os.path.abspath(extractedData[3])
    return extractedData

#
# Create the folder tree
#
def createFolderTree(destination, city, make, model):
    # English
    path = destination + os.sep + enRoot
    if not os.path.exists(path):
        os.makedirs(path)
    if not os.path.exists(path + os.sep + enSubQualifier):
        os.makedirs(path + os.sep + enSubQualifier)
    path += os.sep + city
    if not os.path.exists(path):
        os.makedirs(path)
    if not os.path.exists(path + os.sep + enSubQualifier):
        os.makedirs(path + os.sep + enSubQualifier)
    path += os.sep + make
    if not os.path.exists(path):
        os.makedirs(path)
    if not os.path.exists(path + os.sep + enSubQualifier):
        os.makedirs(path + os.sep + enSubQualifier)
    path += os.sep + model
    if not os.path.exists(path):
        os.makedirs(path)
    if not os.path.exists(path + os.sep + enSubQualifier):
        os.makedirs(path + os.sep + enSubQualifier)

    # French
    path = destination + os.sep + frRoot
    if not os.path.exists(path):
        os.makedirs(path)
    if not os.path.exists(path + os.sep + frSubQualifier):
        os.makedirs(path + os.sep + frSubQualifier)
    path += os.sep + city
    if not os.path.exists(path):
        os.makedirs(path)
    if not os.path.exists(path + os.sep + frSubQualifier):
        os.makedirs(path + os.sep + frSubQualifier)
    path += os.sep + make
    if not os.path.exists(path):
        os.makedirs(path)
    if not os.path.exists(path + os.sep + frSubQualifier):
        os.makedirs(path + os.sep + frSubQualifier)
    path += os.sep + model
    if not os.path.exists(path):
        os.makedirs(path)
    if not os.path.exists(path + os.sep + frSubQualifier):
        os.makedirs(path + os.sep + frSubQualifier)

#
# Copy the file content after having replaced the specified text
#    
def replaceText(sourceFilename, targetFilename, searchedText, replacementText):
    # Source: http://docs.python.org/py3k/howto/unicode.html#reading-and-writing-unicode-data
    source = open(sourceFilename, mode='rt', encoding='utf-8', newline='\n')
    target = open(targetFilename, mode='wt', encoding='utf-8', newline='\n')
    for s in source:
        target.write(s.replace(searchedText, replacementText))
    source.close()
    target.close()

#
# Function replacing the meta-tags
#
def replaceMetaTags(source, city, postalCode, qualifier, make, model, otherLanguagePath):
    homedir = os.path.expanduser("~") + os.sep
    replaceText(source, homedir + "_1.tmp", "_CITY_", city)                             # .replace("é", "&eacute;")
    replaceText(homedir + "_1.tmp", homedir + "_2.tmp", "_POSTAL_CODE_", postalCode)
    replaceText(homedir + "_2.tmp", homedir + "_3.tmp", "_QUALIFIER_", qualifier)       # .replace("é", "&eacute;")
    replaceText(homedir + "_3.tmp", homedir + "_4.tmp", "_MAKE_", make)
    replaceText(homedir + "_4.tmp", homedir + "_5.tmp", "_MODEL_", model)
    replaceText(homedir + "_5.tmp", source, "_OTHER_LANGUAGE_PATH_", otherLanguagePath) # .replace("é", "&eacute;")

def enCopyAndUpdate(baseFilename, path, city, postalCode, qualifier, make, model, otherPath):
    shutil.copyfile (baseFilename + "_en.html", path + os.sep + "index.html")
    replaceMetaTags (path + os.sep + "index.html", city, postalCode, qualifier, make, model, otherPath + "/index.html")

def frCopyAndUpdate(baseFilename, path, city, postalCode, qualifier, make, model, otherPath):
    shutil.copyfile (baseFilename + "_fr.html", path + os.sep + "index.html")
    replaceMetaTags (path + os.sep + "index.html", city, postalCode, qualifier, make, model, otherPath + "/index.html")

#
# Function creating the folder city/make/model and copying and customizing the templates in sub-folders
#
def processCityMakeModel(baseFilename, destination, city, postalCode, make, model):
    enCopyAndUpdate(baseFilename, destination + os.sep + enRoot + os.sep + city + os.sep + make + os.sep + model,                           city, postalCode, enTopQualifier, make, model, "/" + frRoot + "/" + city + "/" + make + "/" + model);
    enCopyAndUpdate(baseFilename, destination + os.sep + enRoot + os.sep + city + os.sep + make + os.sep + model + os.sep + enSubQualifier, city, postalCode, enSubQualifier, make, model, "/" + frRoot + "/" + city + "/" + make + "/" + model + "/" + frSubQualifier);

    frCopyAndUpdate(baseFilename, destination + os.sep + frRoot + os.sep + city + os.sep + make + os.sep + model,                           city, postalCode, frTopQualifier,  make, model, "/" + enRoot + "/" + city + "/" + make + "/" + model);
    frCopyAndUpdate(baseFilename, destination + os.sep + frRoot + os.sep + city + os.sep + make + os.sep + model + os.sep + frSubQualifier, city, postalCode, frNiceQualifier, make, model, "/" + enRoot + "/" + city + "/" + make + "/" + model + "/" + enSubQualifier);

#
# Function creating the folder city/make/defaultModel and copying and customizing the templates in sub-folders
#
def processCityMake(baseFilename, destination, city, postalCode, make, defaultModel):
    enCopyAndUpdate(baseFilename, destination + os.sep + enRoot + os.sep + city + os.sep + make,                           city, postalCode, enTopQualifier, make, defaultModel, "/" + frRoot + "/" + city + "/" + make);
    enCopyAndUpdate(baseFilename, destination + os.sep + enRoot + os.sep + city + os.sep + make + os.sep + enSubQualifier, city, postalCode, enSubQualifier, make, defaultModel, "/" + frRoot + "/" + city + "/" + make + "/" + frSubQualifier);

    frCopyAndUpdate(baseFilename, destination + os.sep + frRoot + os.sep + city + os.sep + make,                           city, postalCode, frTopQualifier,  make, defaultModel, "/" + enRoot + "/" + city + "/" + make);
    frCopyAndUpdate(baseFilename, destination + os.sep + frRoot + os.sep + city + os.sep + make + os.sep + frSubQualifier, city, postalCode, frNiceQualifier, make, defaultModel, "/" + enRoot + "/" + city + "/" + make + "/" + enSubQualifier);

#
# Function creating the folder city/defaultMake and copying and customizing the templates in sub-folders
#
def processCity(baseFilename, destination, city, postalCode):
    enCopyAndUpdate(baseFilename, destination + os.sep + enRoot + os.sep + city,                           city, postalCode, enTopQualifier, enDefaultMake, "", "/" + frRoot + "/" + city);
    enCopyAndUpdate(baseFilename, destination + os.sep + enRoot + os.sep + city + os.sep + enSubQualifier, city, postalCode, enSubQualifier, enDefaultMake, "", "/" + frRoot + "/" + city + "/" + frSubQualifier);

    frCopyAndUpdate(baseFilename, destination + os.sep + frRoot + os.sep + city,                           city, postalCode, frTopQualifier,  frDefaultMake, "", "/" + enRoot + "/" + city);
    frCopyAndUpdate(baseFilename, destination + os.sep + frRoot + os.sep + city + os.sep + frSubQualifier, city, postalCode, frNiceQualifier, frDefaultMake, "", "/" + enRoot + "/" + city + "/" + enSubQualifier);

#
# Function creating the folder defaultCity and copying and customizing the templates in sub-folders
#
def processRoot(baseFilename, destination):
    enCopyAndUpdate(baseFilename, destination + os.sep + enRoot,                           enDefaultCity, "", enTopQualifier, enDefaultMake, "", "/" + frRoot);
    enCopyAndUpdate(baseFilename, destination + os.sep + enRoot + os.sep + enSubQualifier, enDefaultCity, "", enSubQualifier, enDefaultMake, "", "/" + frRoot + os.sep + frSubQualifier);

    frCopyAndUpdate(baseFilename, destination + os.sep + frRoot,                           frDefaultCity, "", frTopQualifier,  frDefaultMake, "", "/" + enRoot);
    frCopyAndUpdate(baseFilename, destination + os.sep + frRoot + os.sep + frSubQualifier, frDefaultCity, "", frNiceQualifier, frDefaultMake, "", "/" + enRoot + "/" + enSubQualifier);

#
# Function propagating the templates for the specified vehicles
#
def propagateTemplates(baseFilename, destination, city, postalCode, vehicles):
    folderNb = 0
    fileNb = 0
    
    # Propagate the templates for the listed vehicles
    for models in vehicles:
        readMake = True
        readFirstModel = True
        for model in models:
            if readMake:
                make = model
                readMake = False
            elif readFirstModel:
                createFolderTree(destination, city, make, model)
                processCityMakeModel(baseFilename, destination, city, postalCode, make, model)
                processCityMake     (baseFilename, destination, city, postalCode, make, model)
                readFirstModel = False
                folderNb += 4
                fileNb += 4
            else:
                # Temporary limitation: stop after having generated the files for the first model
                break
                createFolderTree(destination, city, make, model)
                processCityMakeModel(baseFilename, destination, city, postalCode, make, model)
                folderNb += 4
                fileNb += 4

    # Propagate the templates for a generic car at the city level
    processCity(baseFilename, destination, city, postalCode)
    folderNb += 2
    fileNb += 4

    # Propagate the templates for the root folder
    processRoot(baseFilename, destination)
    folderNb += 2
    fileNb += 4
    
    return [folderNb, fileNb]
