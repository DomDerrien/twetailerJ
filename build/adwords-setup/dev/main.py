#! /usr/bin/env python
# -*- coding: UTF-8 -*-

import os, sys, time, getopt, io, datetime, string, re
import params, twetailer.csv, twetailer.tmpl

#
# Print the program usage
#
def usage():
    print("Usage:")
    print("  -w, --webDir  Folder where the templates will be propagated")
    print("  -c, --csvDir  Folder where the CSV file for AdWords will be saved -- Default: current directory")
    print("  -n, --newCSV  To generate a new CSV file name at each run -- Default: False")
    print("  -s, --silent  To bypass the prompt asking for a confirmation of the propagation -- Default: False")
    print("")

#
# Extract the program parameters
#
def extractParameters(arguments):
    extractedData = dict()
    extractedData['csvDir'] = '.'
    extractedData['newCSV'] = False
    extractedData['silent'] = False
    try:
        optionList, optionValues = getopt.getopt(arguments, "w:c:n:s", ["webDir=", "csvDir", "newCSV", "silent"])
    except getopt.GetoptError:
        usage()
        sys.exit(2)
    for option, value in optionList:
        if   option in ["-w", "--webDir"]: extractedData['webDir'] = value
        elif option in ["-c", "--csvDir"]: extractedData['csvDir'] = value
        elif option in ["-n", "--newCSV"]: extractedData['newCSV'] = value
        elif option in ["-s", "--silent"]: extractedData['silent'] = value
    if not 'webDir' in extractedData:
        usage()
        sys.exit(2)
    if not os.path.exists(extractedData['webDir']):
        print("Destination path '" + extractedData['webDir'] + "' does not exist!")
        print("")
        usage()
        sys.exit()
    if not os.path.exists(extractedData['csvDir']):
        print("Destination path '" + extractedData['csvDir'] + "' does not exist!")
        print("")
        usage()
        sys.exit()
    extractedData['webDir'] = os.path.abspath(extractedData['webDir'])
    extractedData['csvDir'] = os.path.abspath(extractedData['csvDir'])
    return extractedData

#
# Write the Ad group in the CSV file and propagate the associate template
#
def writeOneAdGroup(parameters, adSubPath, altAdSubPath, patterns):
    # 1. Update the pattern instance with the destination and alternate URLS
    rootFolder = parameters['rootFolder']
    baseFilenames = parameters['baseFilenames']
    destinationUrl = '/' + rootFolder['root'] + '/' + adSubPath + baseFilenames[0] + '.html'
    altDestinationUrl = '/' + rootFolder['alt'] + '/' + altAdSubPath + baseFilenames[0] + '.html'
    patterns.set(altPageUrl = altDestinationUrl, adTextGroup = parameters['adTextGroup'])

    # 2. Write the definition of the Text Ad in the CSV file
    parameters['csv'].write(twetailer.csv.getAdTextDefinition(
        parameters['campaignName'],
        parameters['adGroupName'],
        patterns.getAdHeadline(),
        patterns.getAdLine1(),
        patterns.getAdLine2(),
        parameters['displayUrl'],
        'http://anothersocialeconomy.com' + destinationUrl
    ))
    
    # 3. Generate the corresponding destination files if needed
    if parameters['generateFiles']:
        for baseFilename in baseFilenames:
            twetailer.tmpl.copyAndUpdate(
                baseFilename,
                parameters['language'],
                parameters['destinationFolder'] + '/' + rootFolder['root'] + '/' + adSubPath,
                patterns
            )
        parameters['fileNb'] += len(baseFilenames)
        
    parameters['adTextGroupNb'] += 1

#
# Loop within the ad groups and write each one
#
def processAllAdGroups(parameters, qualifierSubPath, altQualifierSubPath, patterns):

    adGroup = string.Template(parameters['adGroup']).safe_substitute(patterns.getDict())
    parameters['adGroupName'] = adGroup

    # 1. Write the ad group
    parameters['csv'].write(twetailer.csv.getAdDefinition(
        parameters['campaignName'],
        adGroup,
        parameters['maxCPC']
    ))
       
    # 2. Write the ad texts
    idx = 0 # required for the look-up in adFolderNames
    defaultAdProcessed = False
    adFolderNames = parameters['adFolderNames']
    for adTextGroup in parameters['ads']:
        parameters['adTextGroup'] = adTextGroup
        adSubPath = qualifierSubPath
        altAdSubPath = altQualifierSubPath
        if not defaultAdProcessed:
            defaultAdProcessed = True
        else:
            adSubPath = qualifierSubPath + adFolderNames[idx] + '/'
            altAdSubPath = altQualifierSubPath + adFolderNames[idx] + '/'

        writeOneAdGroup(parameters, adSubPath, altAdSubPath, patterns)
        
        idx += 1
    
    # 3. Write the ad keywords
    parameters['csv'].write(twetailer.csv.getAdKeyword(
        parameters['campaignName'],
        adGroup,
        parameters['maxCPC'],
        string.Template(parameters['keyword']).safe_substitute(patterns.getDict()),
        parameters['keywordType']
    ))
        
    parameters['adGroupNb'] += 1
    
#
# Loop within the qualifier list and pass it to the function processing the ad groups
#
def processAllQualifiers(parameters, modelSubPath, patterns):
    defaultQualifierProcessed = False
    for qualifier in parameters['qualifiers']:
        parameters['qualifier'] = qualifier
        patterns.set(qualifier = qualifier['keyword'], printedQualifier = qualifier['label'])
        
        qualifierSubPath = modelSubPath
        altQualifierSubPath = modelSubPath
        if not defaultQualifierProcessed:
            defaultQualifierProcessed = True
        else:
            qualifierSubPath = modelSubPath + twetailer.tmpl.stripAccents(qualifier['keyword']) + '/'
            altQualifierSubPath = modelSubPath + twetailer.tmpl.stripAccents(qualifier['alt']) + '/'

        processAllAdGroups(parameters, qualifierSubPath, altQualifierSubPath, patterns)

#
# Loop within the model list and pass it to the function processing the qualifier list
#
def processAllModels(parameters, makeSubPath, patterns):
    # 1. Define the ads for each model
    for model in parameters['models']:
        parameters['model'] = model
        patterns.set(model = model)
        
        processAllQualifiers(parameters, makeSubPath + twetailer.tmpl.stripAccents(model) + '/', patterns)

    # 2. Generate the default files at the make level with a default model
    adTextGroup = parameters['ads'][0]
    rootFolder = parameters['rootFolder']
    baseFilenames = parameters['baseFilenames']
    patterns.set(
        adTextGroup = adTextGroup,
        altPageUrl = '/' + rootFolder['alt'] + '/' + makeSubPath + baseFilenames[0] + '.html',
        model = parameters['models'][0]
    )
    
    for baseFilename in baseFilenames:
        twetailer.tmpl.copyAndUpdate(
            baseFilename,
            parameters['language'],
            parameters['destinationFolder'] + '/' + rootFolder['root'] + '/' + makeSubPath,
            patterns
        )
    parameters['fileNb'] += len(baseFilenames)
    
#
# Loop within the make list and pass it to the function processing the model list
#
def processAllMakes(parameters, citySubPath, patterns):
    # 1. Define the ads for each make
    for makeAndModels in parameters['makesAndModels']:
        parameters['make'] = makeAndModels['make']
        parameters['models'] = makeAndModels['models']
        patterns.set(make = makeAndModels['make'])
        
        processAllModels(parameters, citySubPath + twetailer.tmpl.stripAccents(makeAndModels['make']) + '/', patterns)

    # 2. Generate the default files at the city level with a default make & model
    adTextGroup = parameters['ads'][0]
    rootFolder = parameters['rootFolder']
    baseFilenames = parameters['baseFilenames']
    patterns.set(
        adTextGroup = adTextGroup,
        altPageUrl = '/' + rootFolder['alt'] + '/' + citySubPath + baseFilenames[0] + '.html',
        make = parameters['makesAndModels'][0]['make'],
        model = parameters['makesAndModels'][0]['models'][0]
    )
    
    for baseFilename in baseFilenames:
        twetailer.tmpl.copyAndUpdate(
            baseFilename,
            parameters['language'],
            parameters['destinationFolder'] + '/' + rootFolder['root'] + '/' + citySubPath,
            patterns
        )
    parameters['fileNb'] += len(baseFilenames)
    
#
# Loop within the city list and pass it to the function processing the make list
#
def processAllCities(parameters, patterns):
    # 1. Define the ads for each city
    parameters['generateFiles'] = True
    for city in parameters['cities']:
        parameters['city'] = city[0] # First element is the city name, second is the default postal code
        parameters['postalCode'] = city[1]
        patterns.set(city = city[0], postalCode = city[1])
                
        processAllMakes(parameters, twetailer.tmpl.stripAccents(city[0]) + '/', patterns)

    # 2. Define the ads at the region level -- the destination URL points towards the files for the first city of the list
    parameters['city'] = parameters['region']
    parameters['adGroup'] = parameters['adGroup'].replace('${CITY}', parameters['region'])
    parameters['keyword'] = parameters['keyword'].replace('${CITY} ', '')
    city = parameters['cities'][0] # Region is directed towards the first city
    parameters['postalCode'] = city[1]
    patterns.set(
        city = city[0],
        postalCode = city[1],
        make = parameters['makesAndModels'][0]['make'],
        model = parameters['makesAndModels'][0]['models'][0]
    )
    
    parameters['generateFiles'] = False # To no generate the files a second time
    processAllMakes(parameters, twetailer.tmpl.stripAccents(city[0]) + '/', patterns)

    # 3. Generate the default files at the root level with a default make & model
    adTextGroup = parameters['ads'][0]
    rootFolder = parameters['rootFolder']
    baseFilenames = parameters['baseFilenames']
    patterns.set(
        adTextGroup = adTextGroup,
        altPageUrl = '/' + rootFolder['alt'] + '/' + baseFilenames[0] + '.html',
        city = parameters['genericCityName'],
        postalCode = '',
        make = parameters['makesAndModels'][0]['make'],
        model = parameters['makesAndModels'][0]['models'][0]
    )
    
    for baseFilename in baseFilenames:
        twetailer.tmpl.copyAndUpdate(
            baseFilename,
            parameters['language'],
            parameters['destinationFolder'] + '/' + rootFolder['root'] + '/',
            patterns
        )
    parameters['fileNb'] += len(baseFilenames)

#
# Loop within the language list,
# Prepare the campaign settings, and
# Pass it to the function processing the city list
#
def processAllLanguages(parameters):
    patterns = params.ReplacementPatterns()
    
    for language in parameters['languages']:
        defaultValues = parameters['defaultValues'][language]
        campaignName = defaultValues[0]
        adGroup = defaultValues[6]

        parameters['csv'].write(twetailer.csv.getCampaignSettings(defaultValues))
        parameters['csv'].write(twetailer.csv.getNegativeKeywords(campaignName, parameters['negativeKeywords'], parameters['negativeKeywordType']))
        
        nestedParameters = {
            'csv':                  parameters['csv'],
            'language':             language,
            'destinationFolder':    parameters['destinationFolder'],
            'rootFolder':           parameters['rootFolders'][language],
            'baseFilenames':        parameters['baseFilenames'],
            'ads':                  parameters['ads'][language],
            'adFolderNames':        parameters['adFolderNames'],
            'adGroup':              adGroup,
            'maxCPC':               defaultValues[7],
            'keyword':              defaultValues[8],
            'keywordType':          defaultValues[9],
            'region':               parameters['regions'][language],
            'cities':               parameters['cities'][language],
            'genericCityName':      parameters['genericCityNames'][language],
            'qualifiers':           parameters['qualifiers'][language],
            'makesAndModels':       parameters['makesAndModels'],
            'campaignName':         campaignName,
            'displayUrl':           defaultValues[13],
            'adGroupNb':            0,
            'adTextGroupNb':        0,
            'fileNb':               0
        }

        processAllCities(nestedParameters, patterns)
        
        parameters['adGroupNb'] += nestedParameters['adGroupNb']
        parameters['adTextGroupNb'] += nestedParameters['adTextGroupNb']
        parameters['fileNb'] += nestedParameters['fileNb']

#
# Main process
#
def main():
#    print(re.sub('^ +', '.', '   bonjour   \n   la\n    compagnie  \n     ', flags=re.M))
#    return

    # Extract the program parameters
    data = extractParameters(sys.argv[1:])
    destinationFolder = data['webDir']
    csvFolder = data['csvDir']
    newCSV = data['newCSV']
    silentMode = data['silent']
    
    # Echo back the given parameters
    print("Prepare the AdWords campaign settings with: ")
    print("  HTML file folder: " + destinationFolder)
    print("  CSV file folder:  " + csvFolder)
    print("  New CVS File:     " + str(newCSV))
    print("")
    
    # Confirm the command with a simple return-to-line or the letter 'y' plus the return-to-line
    if silentMode == False:
        yn = input("Do you want to continue? [y/N]")
        if yn != "" and yn != "y" and yn != "Y":
            print("Bye")
            print("")
            sys.exit()
    startTime = time.clock()
    
    # Get parameters
    languages = params.getLanguages()
    rootFolders = params.getRootFolderNames()
    cities = params.getCities()
    makesAndModels = params.getMakesModels()
    qualifiers = params.getQualifiers()
    ads = params.getAds()
    adFolderNames = params.getAdFolderNames()
    
    # Create the folder tree
    twetailer.tmpl.createFolderTree(
        destinationFolder,
        languages,
        rootFolders,
        cities,
        makesAndModels,
        qualifiers,
        ads,
        adFolderNames
    )
    
    # Prepare the CSV header
    csv = io.StringIO(newline='\n')
    csv.write(twetailer.csv.getHeaders(params.getColumnNames()))
    
    # Get parameters
    regions = params.getRegions()
    genericCityNames = params.getGenericCityNames()
    defaultValues = params.getDefaultValues()
    baseFilenames = params.getBaseFilenames()
    negativeKeywords = params.getNegativeKeywords()
    negativeKeywordType = params.getNegativeKeywordType()
    
    # Generate the CSV lines and propagate the templates
    parameters = {
        'csv':                  csv,
        'languages':            languages,
        'destinationFolder':    destinationFolder,
        'rootFolders':          rootFolders,
        'baseFilenames':        baseFilenames,
        'ads':                  ads,
        'adFolderNames':        adFolderNames,
        'defaultValues':        defaultValues,
        'negativeKeywords':     negativeKeywords,
        'negativeKeywordType':  negativeKeywordType,
        'regions':              regions,
        'cities':               cities,
        'genericCityNames':     genericCityNames,
        'qualifiers':           qualifiers,
        'makesAndModels':       makesAndModels,
        'adGroupNb':            0,
        'adTextGroupNb':        0,
        'fileNb':               0
    }
    processAllLanguages(parameters)
    
    # Persist the csv on disk
    csv.seek(0)
    crazyAmount = 1024*1024*1024
    # Source: http://docs.python.org/py3k/howto/unicode.html#reading-and-writing-unicode-data
    if newCSV:
        csvFilename = csvFolder + '/' + datetime.datetime.now().strftime("%Y-%m-%dT%H:%M:%S") + '.csv'
    else:
        csvFilename = csvFolder + '/ase-campaign.csv'
    target = open(csvFilename, mode='wt', encoding='iso-8859-1', newline='\r\n')
    target.write(csv.read(crazyAmount))
    target.close()
    
    # Echo the process time
    print("Time spent is %.3f seconds" % (time.clock()-startTime))
    print("Ad Group Nb:      " + str(parameters['adGroupNb']))
    print("Ad Text Group Nb: " + str(parameters['adTextGroupNb']))
    print("File Nb:          " + str(parameters['fileNb']))
    print("")
    
main()
