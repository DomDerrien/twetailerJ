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
        ["Buick",         "Allure", "Century", "Enclave", "GNX", "Gran Sport", "Grand National", "LaCrosse", "LeSabre", "Lucerne", "Park Avenue", "Rainier", "Reatta", "Regal", "Rendezvous", "Riviera", "Roadmaster", "Skylark", "Terraza"],
        ["Cadillac",      "Allanté", "Coupe DeVille", "CTS", "CTS-V", "DeVille", "DTS", "Eldorado", "Escalade", "Escalade ESV", "Escalade EXT", "Fleetwood", "Seville", "SRX", "STS", "STS-V", "XLR"],
        ["Chevrolet",     "1500 Pickup", "2500 Pickup", "3500 Pickup", "Astro", "Avalanche", "Aveo", "Blazer", "Camaro", "Cavalier", "Cobalt", "Colorado", "Corvette", "Epica", "Equinox", "Express Van", "HHR", "Impala", "Malibu", "Malibu Maxx", "Monte Carlo", "Optra", "S10 Pickup ", "Silverado 1500 Pickup", "Silverado 2500 Pickup", "Silverado 3500 Pickup", "Suburban", "Tahoe", "Tracker", "TrailBlazer", "Traverse", "Uplander", "Venture"],
        ["Chrysler",      "200", "300", "300C", "300M", "Aspen", "Cirrus", "Concorde", "Crossfire", "Imperial", "Intrepid ", "LeBaron", "Neon", "New Yorker", "Pacifica", "Prowler", "PT Cruiser", "Sebring", "Town & Country"],
        ["Dodge",         "Avenger", "Caliber", "Caravan", "Challenger", "Charger", "Coronet", "Dakota", "Dart", "Daytona", "Durango", "Grand Caravan", "Journey", "Magnum", "Neon", "Neon SRT-4", "Nitro", "RAM", "Ram 1500 Pickup", "Ram 2500", "Ram 2500 Pickup", "Ram 3500", "Ram 3500 Pickup", "Ram 5500", "Ram 5500 Cab-Chassis", "Ram SRT-10", "Ram Van", "Sprinter 2500", "Stealth", "Stratus", "SX", "Viper"],
        ["Ford",          "Focus", "Club Wagon","Contour","Coupe","Econoline","Edge", "Escape", "E-Series", "E-Series Wagon", "Expedition", "Explorer", "Explorer Sport Trac", "F-150", "F-250", "F-350", "F-450", "F-550", "F-650", "Fiesta", "Five Hundred", "Flex", "Freestar", "Freestyle", "Fusion", "Galaxie", "Mustang", "Ranger", "Taurus", "Taurus X", "Thunderbird", "Torino", "Transit Connect", "Windstar"],
        ["GMC",           "150 Pickup", "1500 Pickup", "2500 Pickup", "3500 Pickup", "5500 Pickup", "Acadia", "C10 Pickup", "Canyon", "Denali", "Envoy", "Envoy XL", "Jimmy", "S15 Jimmy", "Safari", "Savana Van", "Sierra 1500", "Sierra 1500 Pickup", "Sierra 2500", "Sierra 2500 Pickup", "Sierra 3500", "Sierra 3500 Pickup", "Sonoma", "Sprint", "Terrain", "Vandura", "Yukon", "Yukon XL"],
        ["Honda",         "Civic", "Accord", "Accord Coupe", "Accord Crosstour", "Accord Hybrid", "Accord Sedan", "Civic Coupe", "Civic GX", "Civic Hybrid", "Civic Sedan", "Civic Si Coupe", "Civic Si Sedan", "CRV", "CRZ", "CR-V", "CR-Z", "Del Sol", "Element", "Element SC", "Fit", "Insight", "N600", "Odyssey", "Passport", "Pilot", "Prelude", "Ridgeline", "S2000", "S600", "S800"],
        ["Hummer",        "H1", "H2", "H3", "H3T"],
        ["Hyundai",       "Accent", "Azera", "Elantra", "Entourage", "Genesis", "Santa Fe", "Sonata", "Tiburon", "Tucson", "Veracruz", "XG350"],
        ["Jeep",          "Cherokee", "Commander", "Compass", "Grand Cherokee", "Liberty", "Patriot", "Wrangler"],
        ["Kia",           "Amanti", "Borrego", "Forte", "Magentis", "Optima", "Rio", "Rio5", "Rondo", "Sedona", "Sephia", "Sorento", "Soul", "Spectra", "Spectra5", "Sportage"],
        ["Infiniti",      "EX", "FX", "G20", "G35", "G37", "G Coupe", "G Sedan", "G Convertible", "G37", "G37 Coupe", "G37x", "G37x AWD", "G37x AWD Coupe", "G37x AWD Sport", "G37 M6", "G37 M6 Sport Coupe", "I30", "I35", "J30", "M", "M30", "M35", "M45", "Q45", "QX", "QX4", "QX56"],
        ["Lamborghini",   "Countach", "Diablo", "Gallardo", "Murchiélago", "Silhouette", "Urraco"],
        ["Land Rover",    "Defender", "Discovery", "Freelander", "LR2", "LR3", "LR4", "Range Rover", "Range Rover Sport"],
        ["Lexus",         "ES 300", "ES 330", "ES 350", "GS 300", "GS 350", "GS 400", "GS 430", "GS 460", "GX 460", "GX 470", "HS 250h", "IS 250", "IS 350", "IS F", "LS 400", "LS 430", "LS 460", "LX 470", "LX 570", "RX", "RX 300", "RX 330", "RX 350", "RX 400h", "SC 430"],
        ["Lincoln",       "Aviator", "Continental", "LS", "Mark III", "Mark LT", "Mark V", "MKS", "MKT", "MKX", "MKZ", "Navigator", "Navigator L", "Town Car", "Zephyr"],
        ["Mazda",         "Mazda3", "626", "B-Series", "CX-7", "CX-9", "Mazda2", "Mazda 2", "Mazda 3", "Mazda3 Sport", "Mazda 3 Sport", "Mazda3 Sedan", "Mazda 3 Sedan", "MazdaSpeed3", "Mazda Speed3", "Mazda Speed 3", "Mazda5", "Mazda 5", "Mazda6", "Mazda6 ", "Mazdaspeed6", "Mazda speed6", "Mazda speed 6", "Miata", "Miata MX-5", "Millenia", "MPV", "Precidia", "Protegé", "Protegé5", "Protege", "Protege5", "RX-7", "RX-8", "Tribute"],
        ["Mercedes-Benz", "B-Class", "C-Class", "C-Class Wagon", "C-Class Sedan", "C-Class Coupe", "CLK", "CLK-Class", "CLK-Class Coupe", "CLK-Cabriolet", "E-Class", "E-Class Sedan", "E-Class Wagon", "E-Class Coupe", "E-Class Cabriolet", "CLS", "CLS-Class", "S", "S-Class", "CL", "CL-Class", "SLK", "SLK-Class", "SL", "SL-Class", "GLK", "GLK-Class", "M", "M-Class", "R", "R-Class", "GL", "GL-Class", "G", "G-Class", "SLR", "SLR-McLaren", "SLS", "Smart", "Sprinter"],
        ["Mini",          "Cooper", "Convertible", "Clubman", "Countryman"],
        ["Mitsubishi",    "Eclipse", "Endeavor", "Evolution", "Galant", "Lancer", "Montero", "Montero Sport", "Outlander", "Raider", "RVR"],
        ["Nissan",        "200SX", "240SX", "300ZX", "350Z", "370Z", "Altima", "Cube", "Frontier", "GT-R", "Juke", "Maxima", "Multi", "Murano", "Pathfinder", "Pathfinder Armada", "Pickup", "Pulsar", "Quest", "Rogue", "Sentra", "Titan", "Versa", "Xterra", "X-Trail"],
        ["Pontiac",       "Aztek", "Bonneville", "Catalina", "Chieftain", "Firebird", "G3", "G5", "G6", "G8", "Grand Am", "Grand Prix", "GTO", "LeMans", "Montana", "Montana SV6", "Parisienne", "Pursuit", "Solstice", "Star Chief", "Sunbird", "Sunfire", "Torrent", "Trans Am", "Trans Sport", "Ventura", "Vibe"],
        ["Porsche",       "911", "912", "928", "930", "968", "Boxster", "Carrera", "Carrera GT", "Cayenne", "Cayman", "Panamera"],
        ["Saab",          "92x", "9-2X", "93", "9-3", "95", "9-5", "96X", "9-6X", "97", "9-7X", "900", "9000"],
        ["Saturn",        "Astra", "Aura", "Ion", "L100", "L200", "L300", "LS1", "LW200", "LW300", "Outlook", "Relay", "SC", "SC1", "SC2", "Sky", "SL", "SL1", "SL2", "SW1", "SW2", "Vue"],
        ["Smart",         "fortwo"],
        ["Subaru",        "Tribeca", "Baja", "Brat", "Justy", "Leone", "Liberty", "Pleo", "Sambar", "Stella", "SVX", "Forester", "Impreza", "Legacy", "Outback", "WRX", "WRX STI", "XT"],
        ["Suzuki",        "Aerio", "Equator", "Esteem", "Grand Vitara", "Kizashi", "Sidekick", "Swift", "SX4", "Verona", "Vitara", "XL7"],
        ["Toyota",        "Corolla", "Tercel", "Echo", "Echo Hatchback", "Yaris", "Yaris Hatchback", "Matrix", "Camry", "Camry Hybrid", "Prius", "Prius Hybrid", "Venza", "Cressida", "Avalon", "MR2", "Paseo", "Celica", "Solara", "Supra", "Previa", "Sienna", "RAV4", "Highlander", "Highlander Hybrid", "4Runner", "Sequoia", ", FJ Cruiser", "Landcruiser", "Tacoma", "T100", "Tundra"],
        ["Volvo",         "C30", "122S", "1800", "245", "544", "850", "940", "960", "C70", "S40", "S60", "S70", "S80", "V40", "V50", "V70", "XC60", "XC70", "XC90"],
        ["Volkswagen",    "Beetle", "Cabrio", "Cabriolet", "Eos", "Eurovan", "Golf", "Golf City", "GTI", "Jetta", "Jetta City", "Jetta Wagon", "New Beetle", "New Beetle Convertible", "Passat", "Passat Wagon", "Phaeton", "Rabbit", "Routan", "Tiguan", "Touareg"],
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
