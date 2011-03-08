#! /usr/bin/env python
# -*- coding: UTF-8 -*-

import os, sys, shutil, getopt, io, string, unicodedata, re

#
# Loop through the ad text list and create the corresponding folder
#
def createAdPaths(parameters, qualifierPath):
    idx = 0 # required for the look-up in adFolderNames
    adFolderNames = parameters['adFolderNames']
    for ad in parameters['ads']:
        if idx != 0:
            adPath = qualifierPath + '/' + adFolderNames[idx]
            if not os.path.exists(adPath):
                adPath = os.path.normpath(adPath)
                os.makedirs(adPath)
        idx += 1
    
#
# Loop through the qualifier list, create the corresponding folder, and pass it to the ad group processor
#
def createQualifierPaths(parameters, modelPath):
    defaultQualifierProcessed = False
    for qualifier in parameters['qualifiers']:
        qualifierPath = modelPath
        if defaultQualifierProcessed:
            qualifierPath = modelPath + '/' + stripAccents(qualifier['keyword'])
            if not os.path.exists(qualifierPath):
                qualifierPath = os.path.normpath(qualifierPath)
                os.makedirs(qualifierPath)
        defaultQualifierProcessed = True
        createAdPaths(parameters, qualifierPath)

#
# Loop through the model list, create the corresponding folder, and pass it to the qualifier processor
#
def createModelPaths(parameters, makePath):
    for model in parameters['models']:
        modelPath = makePath + '/' + stripAccents(model)
        if not os.path.exists(modelPath):
            modelPath = os.path.normpath(modelPath)
            os.makedirs(modelPath)
        createQualifierPaths(parameters, modelPath)
    
#
# Loop through the make list, create the corresponding folder, and pass it to the model processor
#
def createMakePaths(parameters, cityPath):
    for makeAndModels in parameters['makesAndModels']:
        make = stripAccents(makeAndModels['make'])
        makePath = cityPath + '/' + make
        makePath = os.path.normpath(makePath)
        if not os.path.exists(makePath):
            os.makedirs(makePath)
        parameters['models'] = makeAndModels['models']
        createModelPaths(parameters, makePath)

#
# Loop through the city list, create the corresponding folder, and pass it to the make processor
#
def createCityPaths(parameters, rootPath):
    for city in parameters['cities']:
        # Neutralize accents in city
        cityPath = rootPath + '/' + stripAccents(city[0]) # First element is the city name, second is the default postal code
        cityPath = os.path.normpath(cityPath)
        if not os.path.exists(cityPath):
            os.makedirs(cityPath)
        createMakePaths(parameters, cityPath)

#
# Create the folder tree
#
def createFolderTree(destination, languages, rootFolders, cities, makesAndModels, qualifiers, ads, adFolderNames):
    for language in languages:
        rootFolder = rootFolders[language]['root']
        rootPath = destination + '/' + rootFolder
        rootPath = os.path.normpath(rootPath)
        
        if not os.path.exists(rootPath):
            rootPath = os.path.normpath(rootPath)
            os.makedirs(rootPath)

        parameters = {
            'cities':           cities[language],
            'makesAndModels':   makesAndModels,
            'qualifiers':       qualifiers[language],
            'ads':              ads[language],
            'adFolderNames':    adFolderNames
        }
        
        createCityPaths(parameters, rootPath)

#
# Global cache for the template contents
#
templateContents = dict()

#
# Function replacing the meta-tags
#
def copyAndUpdate(baseFilename, language, destinationFolder, patterns):
    sourceFilename = baseFilename + '_' + language + '.html'

    # Check the cache
    if sourceFilename in templateContents:
        # Get from the cache
        content = templateContents[sourceFilename]
    else:
        crazyAmount = 1024 * 1024
        # Source: http://docs.python.org/py3k/howto/unicode.html#reading-and-writing-unicode-data
        source = open(sourceFilename, mode='rt', encoding='utf-8', newline='\n')
        content = source.read(crazyAmount)
        source.close()
        # Inject the localModule.js file content
        content = content.replace('${LOCAL_MODULE}', getLocalModuleCode())
        # Remove the comments in JavaScript code, especially identified by '// **'
        content = re.sub('// \*\*.*$', '', content, flags=re.M)
        # Remove the leading spaces and forgotten trailing ones
        content = re.sub('^\s+', '', content, flags=re.M)
        content = re.sub('\s+$', '', content, flags=re.M)
        # Save into the cache
        templateContents[sourceFilename] = content

    # Replace the tags
    content = string.Template(content).safe_substitute(patterns.getDict())

    # Persist the update template content
    # Source: http://docs.python.org/py3k/howto/unicode.html#reading-and-writing-unicode-data
    target = open(destinationFolder + baseFilename + '.html', mode='wt', encoding='utf-8', newline='\n')
    target.write(content)
    target.close()

def getLocalModuleCode():
    sourceFilename = 'localModule.js'

    # Check the cache
    if sourceFilename in templateContents:
        # Get from the cache
        content = templateContents[sourceFilename]
    else:
        crazyAmount = 1024 * 1024
        # Source: http://docs.python.org/py3k/howto/unicode.html#reading-and-writing-unicode-data
        source = open(sourceFilename, mode='rt', encoding='utf-8', newline='\n')
        content = source.read(crazyAmount)
        source.close()
        # Save into the cache
        templateContents[sourceFilename] = content
        
    return content

#
# Global cache for words with stripped accents
#
strippedAccents = dict()

#
# Remove accents
#
def stripAccents(source):
    if source in strippedAccents:
        target = strippedAccents[source]
    else:
        target = ''.join((c for c in unicodedata.normalize('NFD', source) if unicodedata.category(c) != 'Mn'))
        strippedAccents[source] = target
    return target
