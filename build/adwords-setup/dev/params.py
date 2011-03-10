#! /usr/bin/env python
# -*- coding: UTF-8 -*-

import os, sys, time, string

#
# Return the base filename list
#
def getBaseFilenames():
    return [ 'index' ]

#
# Return the language list
#
def getLanguages():
    return [ 'en', 'fr' ]

#
# Return the root folder names
#
def getRootFolderNames():
    return {
        'en': { 'root': 'CarDealers', 'alt': 'Automobiles' },
        'fr': { 'root': 'Automobiles', 'alt': 'CarDealers' }
    }
    
#
# Return the region list
#
def getRegions():
    return {
        'en': 'Montreal Area',
        'fr': 'Région de Montréal'
    }

#
# Return the default city labels for the root level
#
def getGenericCityNames():
    return {
        'en': 'Your Local',
        'fr': 'votre localité'
    }
    
#
# Return the city list
#
def getCities():
    return {
        'en': [
            [ "Montreal", "H2Y 1C6" ],
            [ "Laval", "H7V 3Z4" ]
        ],
        'fr': [
            [ "Montréal", "H2Y 1C6" ],
            [ "Laval", "H7V 3Z4" ]
        ]
    }

#
# Return the qualifier list
#
def getQualifiers():
    return {
        'en': [
            { 'keyword': "PreOwned", 'label': "Pre-Owned", 'alt': "Occasion" },
            { 'keyword': "Used", 'label': "Used", 'alt': "Usagee" },
        ],
        'fr': [
            { 'keyword': "Occasion", 'label': "d'Occasion", 'alt': "PreOwned" },
            { 'keyword': "Usagée", 'label': "Usagée", 'alt': "Used" },
        ]
    }

#
# Return the list of columns for the CSV file to be consumed by AdWords
#
def getColumnNames():
    return [
        "Campaign Name",     # 0
        "Daily Budget",      # 1
        "Language",          # 2
        "Location",          # 3
        "Proximity Targets", # 4
        "Ad Schedule",       # 5
        "Ad Group",          # 6
        "Max CPC",           # 7
        "Keyword",           # 8
        "Keyword Type",      # 9
        "Headline",          # 10
        "Description Line 1",# 11
        "Description Line 2",# 12
        "Display URL",       # 13
        "Destination URL",   # 14
        "Campaign Status",   # 15
        "AdGroup Status",    # 16
        "Ad Status",         # 17
        "Keyword Status"     # 18
    ]
    
#
# Return the default values for most of the cells of the CSV file
#
def getDefaultValues():
    return {
        'en': [
            "Car Dealers Montreal Area",                            # 0. Campaign Name
            "10",                                                   # 1. Campaign Daily Budget
            "en",                                                   # 2. Language
            "CA",                                                   # 3. Location
            "(35km:45.508889:-73.554166)",                          # 4. Proximity Targets
            "[]",                                                   # 5. Ad Schedule
            "${QUALIFIER} ${CITY} ${MAKE} ${MODEL}",                # 6. Ad Group
            "",                                                     # 7. Max CPC
            "+${QUALIFIER} ${CITY} ${MAKE} +${MODEL}",              # 8. Keyword
            "Broad",                                                # 9. Keyword Type
            None,                                                   # 10. Headline
            None,                                                   # 11. Description Line 1
            None,                                                   # 12. Description Line 2
            "AnotherSocialEconomy.com/CarDealers",                  # 13. Display URL
            None                                                    # 14. Destination URL
        ],
        'fr': [
            "Concessionnaires Région de Montréal",                  # 0. Campaign Name
            "10",                                                   # 1. Campaign Daily Budget
            "fr",                                                   # 2. Language
            "CA",                                                   # 3. Location
            "(35km:45.508889:-73.554166)",                          # 4. Proximity Targets
            "[]",                                                   # 5. Ad Schedule
            "${CITY} ${MAKE} ${MODEL} ${QUALIFIER}",                # 6. Ad Group
            "",                                                     # 7. Max CPC
            "${CITY} ${MAKE} +${MODEL} +${QUALIFIER}",              # 8. Keyword
            "Broad",                                                # 9. Keyword Type
            None,                                                   # 10. Headline
            None,                                                   # 11. Description Line 1
            None,                                                   # 12. Description Line 2
            "AnotherSocialEconomy.com/Automobile",                  # 13. Display URL
            None                                                    # 14. Destination URL
        ]
    }

#
# Return the list of negative keywords
#
def getNegativeKeywords():
    return [
        "credit",
        "finance",
        "financement",
        "garage",
        "garages",
        "assurance",
        "location",
        "prêt",
        "nouveau",
        "neuve",
        "neuf",
        "opinion",
        "opinions",
        "pièce",
        "pièces",
        "réparation",
        "réparations",
        "review",
        "reviews",
        "service",
        "camion",
        "camions"
    ]

#
# Return the type for the negative keywords
#
def getNegativeKeywordType():
    return "Campaign Negative Broad"

#
# Return the list of ad text groups
#
def getAds():
    """ Be careful:
        1. getAdFolerNames() below MUST return enough names to cover the highest number of ads listed in this function!
        2. same number of ads in English as in French, to allow the links to the other languages to work transparently
    """
    return {
        'en': [
            {
                'headline': [ "${QUALIFIER} ${CITY} ${MAKE} ${MODEL}s", "${CITY} ${MAKE} ${MODEL}s", "${MAKE} ${MODEL}s", "${MODEL}s" ],
                'line1': "Reverse the Search! Get Pre-owned",
                'line2': "Car Proposals from Rated Dealers",
                #         12345678901234567890123456789012345
            },
            {
                'headline': [ "${QUALIFIER} ${CITY} ${MAKE} ${MODEL}s", "${CITY} ${MAKE} ${MODEL}s", "${MAKE} ${MODEL}s", "${MODEL}s" ],
                'line1': "Get Confidential Unadvertised Deals",
                'line2': "From Montreal Area Dealerships Now",
            },
            {
                'headline': [ "${QUALIFIER} ${CITY} ${MAKE} ${MODEL}s", "${CITY} ${MAKE} ${MODEL}s", "${MAKE} ${MODEL}s", "${MODEL}s" ],
                'line1': "Receive Local Dealer Deals In Your",
                'line2': "Inbox. Reverse the Search for Free!",
            }
        ],
        'fr': [
            {
                'headline': [ "${MAKE} ${MODEL} ${QUALIFIER} à ${CITY}", "${MAKE} ${MODEL} ${QUALIFIER}", "${MAKE} ${MODEL} à ${CITY}", "${MAKE} ${MODEL}", "${MODEL}" ],
                'line1': "Inverser la recherche: décrivez",
                'line2': "la voiture et recevez les offres"
                #         12345678901234567890123456789012345
            },
            {
                'headline': [ "${MAKE} ${MODEL} ${QUALIFIER} à ${CITY}", "${MAKE} ${MODEL} ${QUALIFIER}", "${MAKE} ${MODEL} à ${CITY}", "${MAKE} ${MODEL}", "${MODEL}" ],
                'line1': "Trouvez la ${MAKE}",
                'line2': "rêvée en quelques clics"
            },
            {
                'headline': [ "${MAKE} ${MODEL} ${QUALIFIER} à ${CITY}", "${MAKE} ${MODEL} ${QUALIFIER}", "${MAKE} ${MODEL} à ${CITY}", "${MAKE} ${MODEL}", "${MODEL}" ],
                'line1': "Trouvez rapidement l'occasion à bon",
                'line2': "prix dans la région de Montréal"
            # },
            # {
            #     'headline': [ "${MAKE} ${MODEL} ${QUALIFIER} à ${CITY}", "${MAKE} ${MODEL} ${QUALIFIER}", "${MAKE} ${MODEL} à ${CITY}", "${MAKE} ${MODEL}", "${MODEL}" ],
            #     'line1': "Trouvez rapidement, sans effort, la",
            #     'line2': "${MAKE} à Montréal"
            }

        ]
    }

def getAdFolderNames():
    return [ 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i' ]

#
# Local cache of the dictionary used for the pattern replacements
#
class ReplacementPatterns:

    def __init__(self):
        self.patterns = dict()
    
    def get(self):
        return patterns
    
    def set(self, city=None, postalCode=None, qualifier=None, printedQualifier=None, make=None, model=None, altPageUrl=None, adTextGroup=None):
        if city != None:             self.patterns['CITY']              = city
        if postalCode != None:       self.patterns['POSTAL_CODE']       = postalCode
        if make != None:             self.patterns['MAKE']              = make
        if model != None:            self.patterns['MODEL']             = model
        if qualifier != None:        self.patterns['QUALIFIER']         = qualifier
        if printedQualifier != None: self.patterns['PRINTED_QUALIFIER'] = printedQualifier
        if altPageUrl != None:       self.patterns['ALT_PATH']          = altPageUrl
        if adTextGroup != None:
            self.injectAdText(adTextGroup)
    
    def injectAdText(self, adTextGroup):
        for headline in adTextGroup['headline']:
             updatedHeadline = string.Template(headline).safe_substitute(self.patterns)
             if len(updatedHeadline) < 26:
                 break
        updatedLine1 = string.Template(adTextGroup['line1']).safe_substitute(self.patterns)
        updatedLine2 = string.Template(adTextGroup['line2']).safe_substitute(self.patterns)
    
        self.patterns['AD_HEADLINE'] = updatedHeadline
        self.patterns['AD_LINE_1'] = updatedLine1
        self.patterns['AD_LINE_2'] = updatedLine2
        self.patterns['AD_LINE'] = updatedLine1 + ' ' + updatedLine2
        
    def getAdHeadline(self): return self.patterns['AD_HEADLINE']
    def getAdLine1(self):    return self.patterns['AD_LINE_1']
    def getAdLine2(self):    return self.patterns['AD_LINE_2']
    def getAdLine(self):     return self.patterns['AD_LINE']
    
    def getDict(self):       return self.patterns
    
#
# Short list of supported makes and models
#
shortMakesAndModels = [
    { 'make': "Acura",         'models': [ "TL", "MDX", "NSX", "RL", "RDX", "RSX", "TSX", "ZDX" ] },
    { 'make': "Audi",          'models': [ "A4", "A3", "A5", "A6", "A8", "Allroad", "Q5", "Q7", "R8", "RS", "S4", "S5", "S6", "TT", "TTS" ] },
    { 'make': "BMW",           'models': [ "335i", "128", "128i", "135", "135i", "318", "318i", "320", "320i", "323", "323i", "325", "325i", "328", "328i", "330", "330i", "335", "525", "525i", "528", "528i", "530", "530i", "535", "535i", "540", "540i", "545", "545i", "550", "550i", "645", "645i", "650", "650i", "735", "735i", "740", "740i", "745", "745i", "750", "750i", "760", "760i", "M3", "M5", "M6", "X3", "X5", "X6", "Z3", "Z4", "Z8" ] },
    { 'make': "Infiniti",      'models': [ "EX", "FX", "G20", "G35", "G37", "G Coupe", "G Sedan", "G Convertible", "G37 Coupe", "G37x", "G37x AWD", "G37x AWD Coupe", "G37x AWD Sport", "G37 M6", "G37 M6 Sport Coupe", "I30", "I35", "J30", "M", "M30", "M35", "M45", "Q45", "QX", "QX4", "QX56" ] },
    { 'make': "Land Rover",    'models': [ "Defender", "Discovery", "Freelander", "LR2", "LR3", "LR4", "Range Rover", "Range Rover Sport" ] },
    { 'make': "Honda",         'models': [ "Civic", "Accord", "Accord Coupe", "Accord Crosstour", "Accord Hybrid", "Accord Sedan", "Civic Coupe", "Civic GX", "Civic Hybrid", "Civic Sedan", "Civic Si Coupe", "Civic Si Sedan", "CRV", "CRZ", "CR-V", "CR-Z", "Del Sol", "Element", "Element SC", "Fit", "Insight", "N600", "Odyssey", "Passport", "Pilot", "Prelude", "Ridgeline", "S2000", "S600", "S800" ] },
    { 'make': "Mercedes-Benz", 'models': [ "B-Class", "C-Class", "C-Class Wagon", "C-Class Sedan", "C-Class Coupe", "CLK", "CLK-Class", "CLK-Class Coupe", "CLK-Cabriolet", "E-Class", "E-Class Sedan", "E-Class Wagon", "E-Class Coupe", "E-Class Cabriolet", "CLS", "CLS-Class", "S", "S-Class", "CL", "CL-Class", "SLK", "SLK-Class", "SL", "SL-Class", "GLK", "GLK-Class", "M", "M-Class", "R", "R-Class", "GL", "GL-Class", "G", "G-Class", "SLR", "SLR-McLaren", "SLS", "Smart", "Sprinter" ] },
    { 'make': "Mini",          'models': [ "Cooper", "Convertible", "Clubman", "Countryman" ] },
    { 'make': "Saab",          'models': [ "92x", "9-2X", "93", "9-3", "95", "9-5", "96X", "9-6X", "97", "9-7X", "900", "9000" ] },
    { 'make': "Volvo",         'models': [ "C30", "122S", "1800", "245", "544", "850", "940", "960", "C70", "S40", "S60", "S70", "S80", "V40", "V50", "V70", "XC60", "XC70", "XC90" ] },
    { 'make': "Volkswagen",    'models': [ "Beetle", "Cabrio", "Cabriolet", "Eos", "Eurovan", "Golf", "Golf City", "GTI", "Jetta", "Jetta City", "Jetta Wagon", "New Beetle", "New Beetle Convertible", "Passat", "Passat Wagon", "Phaeton", "Rabbit", "Routan", "Tiguan", "Touareg" ] }
]

#
# Full list of supported makes and models
#
#
fullMakesAndModels = [
    { 'make': "Acura",         'models': [ "TL", "MDX", "NSX", "RL", "RDX", "RSX", "TSX", "ZDX" ] },
    { 'make': "Audi",          'models': [ "A4", "A3", "A5", "A6", "A8", "Allroad", "Q5", "Q7", "R8", "RS", "S4", "S5", "S6", "TT", "TTS" ] },
    { 'make': "BMW",           'models': [ "335i", "128", "128i", "135", "135i", "318", "318i", "320", "320i", "323", "323i", "325", "325i", "328", "328i", "330", "330i", "335", "525", "525i", "528", "528i", "530", "530i", "535", "535i", "540", "540i", "545", "545i", "550", "550i", "645", "645i", "650", "650i", "735", "735i", "740", "740i", "745", "745i", "750", "750i", "760", "760i", "M3", "M5", "M6", "X3", "X5", "X6", "Z3", "Z4", "Z8" ] },
    { 'make': "Buick",         'models': [ "Allure", "Century", "Enclave", "GNX", "Gran Sport", "Grand National", "LaCrosse", "LeSabre", "Lucerne", "Park Avenue", "Rainier", "Reatta", "Regal", "Rendezvous", "Riviera", "Roadmaster", "Skylark", "Terraza" ] },
    { 'make': "Cadillac",      'models': [ "Allanté", "Coupe DeVille", "CTS", "CTS-V", "DeVille", "DTS", "Eldorado", "Escalade", "Escalade ESV", "Escalade EXT", "Fleetwood", "Seville", "SRX", "STS", "STS-V", "XLR" ] },
    { 'make': "Chevrolet",     'models': [ "1500 Pickup", "2500 Pickup", "3500 Pickup", "Astro", "Avalanche", "Aveo", "Blazer", "Camaro", "Cavalier", "Cobalt", "Colorado", "Corvette", "Epica", "Equinox", "Express Van", "HHR", "Impala", "Malibu", "Malibu Maxx", "Monte Carlo", "Optra", "S10 Pickup", "Silverado 1500 Pickup", "Silverado 2500 Pickup", "Silverado 3500 Pickup", "Suburban", "Tahoe", "Tracker", "TrailBlazer", "Traverse", "Uplander", "Venture" ] },
    { 'make': "Chrysler",      'models': [ "200", "300", "300C", "300M", "Aspen", "Cirrus", "Concorde", "Crossfire", "Imperial", "Intrepid", "LeBaron", "Neon", "New Yorker", "Pacifica", "Prowler", "PT Cruiser", "Sebring", "Town & Country" ] },
    { 'make': "Dodge",         'models': [ "Avenger", "Caliber", "Caravan", "Challenger", "Charger", "Coronet", "Dakota", "Dart", "Daytona", "Durango", "Grand Caravan", "Journey", "Magnum", "Neon", "Neon SRT-4", "Nitro", "RAM", "Ram 1500 Pickup", "Ram 2500", "Ram 2500 Pickup", "Ram 3500", "Ram 3500 Pickup", "Ram 5500", "Ram 5500 Cab-Chassis", "Ram SRT-10", "Ram Van", "Sprinter 2500", "Stealth", "Stratus", "SX", "Viper" ] },
    { 'make': "Ford",          'models': [ "Focus", "Club Wagon","Contour","Coupe","Econoline","Edge", "Escape", "E-Series", "E-Series Wagon", "Expedition", "Explorer", "Explorer Sport Trac", "F-150", "F-250", "F-350", "F-450", "F-550", "F-650", "Fiesta", "Five Hundred", "Flex", "Freestar", "Freestyle", "Fusion", "Galaxie", "Mustang", "Ranger", "Taurus", "Taurus X", "Thunderbird", "Torino", "Transit Connect", "Windstar" ] },
    { 'make': "GMC",           'models': [ "150 Pickup", "1500 Pickup", "2500 Pickup", "3500 Pickup", "5500 Pickup", "Acadia", "C10 Pickup", "Canyon", "Denali", "Envoy", "Envoy XL", "Jimmy", "S15 Jimmy", "Safari", "Savana Van", "Sierra 1500", "Sierra 1500 Pickup", "Sierra 2500", "Sierra 2500 Pickup", "Sierra 3500", "Sierra 3500 Pickup", "Sonoma", "Sprint", "Terrain", "Vandura", "Yukon", "Yukon XL" ] },
    { 'make': "Honda",         'models': [ "Civic", "Accord", "Accord Coupe", "Accord Crosstour", "Accord Hybrid", "Accord Sedan", "Civic Coupe", "Civic GX", "Civic Hybrid", "Civic Sedan", "Civic Si Coupe", "Civic Si Sedan", "CRV", "CRZ", "CR-V", "CR-Z", "Del Sol", "Element", "Element SC", "Fit", "Insight", "N600", "Odyssey", "Passport", "Pilot", "Prelude", "Ridgeline", "S2000", "S600", "S800" ] },
    { 'make': "Hummer",        'models': [ "H1", "H2", "H3", "H3T" ] },
    { 'make': "Hyundai",       'models': [ "Accent", "Azera", "Elantra", "Entourage", "Genesis", "Santa Fe", "Sonata", "Tiburon", "Tucson", "Veracruz", "XG350" ] },
    { 'make': "Jeep",          'models': [ "Cherokee", "Commander", "Compass", "Grand Cherokee", "Liberty", "Patriot", "Wrangler" ] },
    { 'make': "Kia",           'models': [ "Amanti", "Borrego", "Forte", "Magentis", "Optima", "Rio", "Rio5", "Rondo", "Sedona", "Sephia", "Sorento", "Soul", "Spectra", "Spectra5", "Sportage" ] },
    { 'make': "Infiniti",      'models': [ "EX", "FX", "G20", "G35", "G37", "G Coupe", "G Sedan", "G Convertible", "G37 Coupe", "G37x", "G37x AWD", "G37x AWD Coupe", "G37x AWD Sport", "G37 M6", "G37 M6 Sport Coupe", "I30", "I35", "J30", "M", "M30", "M35", "M45", "Q45", "QX", "QX4", "QX56" ] },
    { 'make': "Lamborghini",   'models': [ "Countach", "Diablo", "Gallardo", "Murchiélago", "Silhouette", "Urraco" ] },
    { 'make': "Land Rover",    'models': [ "Defender", "Discovery", "Freelander", "LR2", "LR3", "LR4", "Range Rover", "Range Rover Sport" ] },
    { 'make': "Lexus",         'models': [ "ES 300", "ES 330", "ES 350", "GS 300", "GS 350", "GS 400", "GS 430", "GS 460", "GX 460", "GX 470", "HS 250h", "IS 250", "IS 350", "IS F", "LS 400", "LS 430", "LS 460", "LX 470", "LX 570", "RX", "RX 300", "RX 330", "RX 350", "RX 400h", "SC 430" ] },
    { 'make': "Lincoln",       'models': [ "Aviator", "Continental", "LS", "Mark III", "Mark LT", "Mark V", "MKS", "MKT", "MKX", "MKZ", "Navigator", "Navigator L", "Town Car", "Zephyr" ] },
    { 'make': "Mazda",         'models': [ "Mazda3", "626", "B-Series", "CX-7", "CX-9", "Mazda2", "Mazda 2", "Mazda 3", "Mazda3 Sport", "Mazda 3 Sport", "Mazda3 Sedan", "Mazda 3 Sedan", "MazdaSpeed3", "Mazda Speed3", "Mazda Speed 3", "Mazda5", "Mazda 5", "Mazda6", "Mazda6", "Mazdaspeed6", "Mazda speed6", "Mazda speed 6", "Miata", "Miata MX-5", "Millenia", "MPV", "Precidia", "Protegé", "Protegé5", "Protege", "Protege5", "RX-7", "RX-8", "Tribute" ] },
    { 'make': "Mercedes-Benz", 'models': [ "B-Class", "C-Class", "C-Class Wagon", "C-Class Sedan", "C-Class Coupe", "CLK", "CLK-Class", "CLK-Class Coupe", "CLK-Cabriolet", "E-Class", "E-Class Sedan", "E-Class Wagon", "E-Class Coupe", "E-Class Cabriolet", "CLS", "CLS-Class", "S", "S-Class", "CL", "CL-Class", "SLK", "SLK-Class", "SL", "SL-Class", "GLK", "GLK-Class", "M", "M-Class", "R", "R-Class", "GL", "GL-Class", "G", "G-Class", "SLR", "SLR-McLaren", "SLS", "Smart", "Sprinter" ] },
    { 'make': "Mini",          'models': [ "Cooper", "Convertible", "Clubman", "Countryman" ] },
    { 'make': "Mitsubishi",    'models': [ "Eclipse", "Endeavor", "Evolution", "Galant", "Lancer", "Montero", "Montero Sport", "Outlander", "Raider", "RVR" ] },
    { 'make': "Nissan",        'models': [ "200SX", "240SX", "300ZX", "350Z", "370Z", "Altima", "Cube", "Frontier", "GT-R", "Juke", "Maxima", "Multi", "Murano", "Pathfinder", "Pathfinder Armada", "Pickup", "Pulsar", "Quest", "Rogue", "Sentra", "Titan", "Versa", "Xterra", "X-Trail" ] },
    { 'make': "Pontiac",       'models': [ "Aztek", "Bonneville", "Catalina", "Chieftain", "Firebird", "G3", "G5", "G6", "G8", "Grand Am", "Grand Prix", "GTO", "LeMans", "Montana", "Montana SV6", "Parisienne", "Pursuit", "Solstice", "Star Chief", "Sunbird", "Sunfire", "Torrent", "Trans Am", "Trans Sport", "Ventura", "Vibe" ] },
    { 'make': "Porsche",       'models': [ "911", "912", "928", "930", "968", "Boxster", "Carrera", "Carrera GT", "Cayenne", "Cayman", "Panamera" ] },
    { 'make': "Saab",          'models': [ "92x", "9-2X", "93", "9-3", "95", "9-5", "96X", "9-6X", "97", "9-7X", "900", "9000" ] },
    { 'make': "Saturn",        'models': [ "Astra", "Aura", "Ion", "L100", "L200", "L300", "LS1", "LW200", "LW300", "Outlook", "Relay", "SC", "SC1", "SC2", "Sky", "SL", "SL1", "SL2", "SW1", "SW2", "Vue" ] },
    { 'make': "Smart",         'models': [ "fortwo" ] },
    { 'make': "Subaru",        'models': [ "Tribeca", "Baja", "Brat", "Justy", "Leone", "Liberty", "Pleo", "Sambar", "Stella", "SVX", "Forester", "Impreza", "Legacy", "Outback", "WRX", "WRX STI", "XT" ] },
    { 'make': "Suzuki",        'models': [ "Aerio", "Equator", "Esteem", "Grand Vitara", "Kizashi", "Sidekick", "Swift", "SX4", "Verona", "Vitara", "XL7" ] },
    { 'make': "Toyota",        'models': [ "Corolla", "Tercel", "Echo", "Echo Hatchback", "Yaris", "Yaris Hatchback", "Matrix", "Camry", "Camry Hybrid", "Prius", "Prius Hybrid", "Venza", "Cressida", "Avalon", "MR2", "Paseo", "Celica", "Solara", "Supra", "Previa", "Sienna", "RAV4", "Highlander", "Highlander Hybrid", "4Runner", "Sequoia", "FJ Cruiser", "Landcruiser", "Tacoma", "T100", "Tundra" ] },
    { 'make': "Volvo",         'models': [ "C30", "122S", "1800", "245", "544", "850", "940", "960", "C70", "S40", "S60", "S70", "S80", "V40", "V50", "V70", "XC60", "XC70", "XC90" ] },
    { 'make': "Volkswagen",    'models': [ "Beetle", "Cabrio", "Cabriolet", "Eos", "Eurovan", "Golf", "Golf City", "GTI", "Jetta", "Jetta City", "Jetta Wagon", "New Beetle", "New Beetle Convertible", "Passat", "Passat Wagon", "Phaeton", "Rabbit", "Routan", "Tiguan", "Touareg" ] }
]

#
# Return the list of makes and models
#
def getMakesModels():
    # return fullMakesAndModels
    return shortMakesAndModels
