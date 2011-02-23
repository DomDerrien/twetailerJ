#! /usr/bin/env python
# -*- coding: UTF-8 -*-

import os, sys, time
from propagate_templates_modules import extractParameters, propagateTemplates

#
# Extract the program parameters
#
data = extractParameters(sys.argv[1:])
city = data[0]
postalCode = data[1]
baseFilename = data[2]
destination = data[3]
silentMode = data[4]

#
# Echo back the given parameters
#
print("Create files with: ")
print("  Source:      " + baseFilename)
print("  Destination: " + destination)
print("  City:        " + city)
print("  Postal Code: " + postalCode)
print("")

#
# Confirm the command with a simple return-to-line or the letter 'y' plus the return-to-line
#
if silentMode == False:
    yn = input("Do you want to continue? [y/N]")
    if yn != "" and yn != "y" and yn != "Y":
        print("Bye")
        print("")
        sys.exit()

#
# Define makes and models
# - one nested array per make
# - first entry of the nested array is the make label
# - second entry of the nested array is the default model for that make
#
makesAndModels = [
        ["Acura",         "TL", "MDX", "NSX", "RL", "RDX", "RSX", "TSX", "ZDX"],
        ["Audi",          "A4", "A3", "A5", "A6", "A8", "Allroad", "Q5", "Q7", "R8", "RS", "S4", "S5", "S6", "TT", "TTS"],
        ["BMW",           "335i", "128", "128i", "135", "135i", "318", "318i", "320", "320i", "323", "323i", "325", "325i", "328", "328i", "330", "330i", "335", "525", "525i", "528", "528i", "530", "530i", "535", "535i", "540", "540i", "545", "545i", "550", "550i", "645", "645i", "650", "650i", "735", "735i", "740", "740i", "745", "745i", "750", "750i", "760", "760i", "M3", "M5", "M6", "X3", "X5", "X6", "Z3", "Z4", "Z8"],
        ["Buick",         "04"],
        ["Cadillac",      "05"],
        ["Chevrolet",     "06"],
        ["Chrysler",      "07"],
        ["Dodge",         "08"],
        ["Ford",          "09"],
        ["GMC",           "10"],
        ["Honda",         "Civic", "Accord", "Accord Coupe", "Accord Crosstour", "Accord Hybrid", "Accord Sedan", "Civic Coupe", "Civic GX", "Civic Hybrid", "Civic Sedan", "Civic Si Coupe", "Civic Si Sedan", "CRV", "CRZ", "CR-V", "CR-Z", "Del Sol", "Element", "Element SC", "Fit", "Insight", "N600", "Odyssey", "Passport", "Pilot", "Prelude", "Ridgeline", "S2000", "S600", "S800"],
        ["Hummer",        "12"],
        ["Hyundai",       "13"],
        ["Jeep",          "14"],
        ["Kia",           "15"],
        ["Infiniti",      "EX", "FX", "G20", "G35", "G37", "G Coupe", "G Sedan", "G Convertible", "G37", "G37 Coupe", "G37x", "G37x AWD", "G37x AWD Coupe", "G37x AWD Sport", "G37 M6", "G37 M6 Sport Coupe", "I30", "I35", "J30", "M", "M30", "M35", "M45", "Q45", "QX", "QX4", "QX56"],
        ["Land Rover",    "Defender", "Discovery", "Freelander", "LR2", "LR3", "LR4", "Range Rover", "Range Rover Sport" ],
        ["Lexus",         "18"],
        ["Lincoln",       "19"],
        ["Mazda",         "20"],
        ["Mercedes-Benz", "B-Class", "C-Class", "C-Class Wagon", "C-Class Sedan", "C-Class Coupe", "CLK", "CLK-Class", "CLK-Class Coupe", "CLK-Cabriolet", "E-Class", "E-Class Sedan", "E-Class Wagon", "E-Class Coupe", "E-Class Cabriolet", "CLS", "CLS-Class", "S", "S-Class", "CL", "CL-Class", "SLK", "SLK-Class", "SL", "SL-Class", "GLK", "GLK-Class", "M", "M-Class", "R", "R-Class", "GL", "GL-Class", "G", "G-Class", "SLR", "SLR-McLaren", "SLS", "Smart", "Sprinter"],
        ["Mini",          "Cooper", "Convertible", "Clubman", "Countryman"],
        ["Mitsubishi",    "23"],
        ["Nissan",        "24"],
        ["Pontiac",       "25"],
        ["Porsche",       "26"],
        ["Saab",          "92x", "9-2X", "93", "9-3", "95", "9-5", "96X", "9-6X", "97", "9-7X", "900", "9000"],
        ["Saturn",        "28"],
        ["Smart",         "29"],
        ["Subaru",        "30"],
        ["Suzuki",        "31"],
        ["Toyota",        "32"],
        ["Volvo",         "C30", "122S", "1800", "245", "544", "850", "940", "960", "C70", "S40", "S60", "S70", "S80", "V40", "V50", "V70", "XC60", "XC70", "XC90"],
        ["Volkswagen",    "Beetle", "Cabrio", "Cabriolet", "Eos", "Eurovan", "Golf", "Golf City", "GTI", "Jetta", "Jetta City", "Jetta Wagon", "New Beetle", "New Beetle Convertible", "Passat", "Passat Wagon", "Phaeton", "Rabbit", "Routan", "Tiguan", "Touareg" ],
    ]         

#
# Iterate over the maker list to create the corresponding files from the templates
#
startTime = time.clock()
stats = propagateTemplates(baseFilename, destination, city, postalCode, makesAndModels)

#
# Print summary
#
if silentMode == False:
    print("Summary:")    
    print("  " + str(len(makesAndModels)) + " makes processed")
    print("  " + str(stats[0]) + " leaf folders created")
    print("  " + str(stats[1]) + " files propagated: ")
    print("  6 string replacements per file (total: " + str(6*stats[1]) + ")")
    print("  Time spent is %.3f seconds" % (time.clock()-startTime))
    print("")
