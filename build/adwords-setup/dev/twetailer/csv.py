#! /usr/bin/env python
# -*- coding: UTF-8 -*-

import os, sys, time

sep = '\t'
eol = '\n'

#
# Return the formatted list of headers
#
def getHeaders(columnNames):
    out = columnNames[0]
    idx = 1
    limit = len(columnNames)
    while(idx < limit):
        out += sep + columnNames[idx]
        idx += 1
    return out + eol

#
# Return the formatted list of campaign settings
#
def getCampaignSettings(defaultValues):
    out = defaultValues[0]        # 0. Campaign Name
    out += sep + defaultValues[1] # 1. Campaign Daily Budget
    out += sep + defaultValues[2] # 2. Language
    out += sep + defaultValues[3] # 3. Geo Targeting
    out += sep + defaultValues[4] # 4. Proximity Targets
    out += sep + defaultValues[5] # 5. Ad Schedule
    out += sep                    # 6. Ad Group
    out += sep                    # 7. Default Max. CPC
    out += sep                    # 8. Keyword
    out += sep                    # 9. Keyword Type
    out += sep                    # 10. Headline
    out += sep                    # 11. Description Line 1
    out += sep                    # 12. Description Line 2
    out += sep                    # 13. Display URL
    out += sep                    # 14. Destination URL
    out += sep + 'Active'         # 15. Campaign Status
    return out + eol

#
# Return the formatted list of negative keywords
#
def getNegativeKeywords(campaignName, negativeKeywords, negativeKeywordType):
    out = ""
    idx = 0
    limit = len(negativeKeywords)
    while idx < limit:
        out += campaignName                # 0. Campaign Name
        out += sep                         # 1. Campaign Daily Budget
        out += sep                         # 2. Language
        out += sep                         # 3. Geo Targeting
        out += sep                         # 4. Proximity Targets
        out += sep                         # 5. Ad Schedule
        out += sep                         # 6. Ad Group
        out += sep                         # 7. Default Max CPC
        out += sep + negativeKeywords[idx] # 8. Keyword
        out += sep + negativeKeywordType   # 9. Keyword Type
        out += sep                         # 10. Headline
        out += sep                         # 11. Description Line 1
        out += sep                         # 12. Description Line 2
        out += sep                         # 13. Display URL
        out += sep                         # 14. Destination URL
        out += sep + 'Active'              # 15. Campaign Status
        out += eol
        idx += 1
    return out

#
# Return the formatted ad group definition
#
def getAdDefinition(campaignName, adGroup, maxCPC):
    out = campaignName    # 0. Campaign Name
    out += sep            # 1. Campaign Daily Budget
    out += sep            # 2. Language
    out += sep            # 3. Geo Targeting
    out += sep            # 4. Proximity Targets
    out += sep            # 5. Ad Schedule
    out += sep + adGroup  # 6. Ad Group
    out += sep + maxCPC   # 7. Default Max. CPC
    out += sep            # 8. Keyword
    out += sep            # 9. Keyword Type
    out += sep            # 10. Headline
    out += sep            # 11. Description Line 1
    out += sep            # 12. Description Line 2
    out += sep            # 13. Display URL
    out += sep            # 14. Destination URL
    out += sep + 'Active' # 15. Campaign Status
    out += sep + 'Active' # 16. Ad Group Status
    return out + eol

#
# Return the formatted ad group definition
#
def getAdTextDefinition(campaignName, adGroup, headline, line1, line2, displayUrl, destinationUrl):
    out = campaignName          # 0. Campaign Name
    out += sep                  # 1. Campaign Daily Budget
    out += sep                  # 2. Language
    out += sep                  # 3. Geo Targeting
    out += sep                  # 4. Proximity Targets
    out += sep                  # 5. Ad Schedule
    out += sep + adGroup        # 6. Ad Group
    out += sep                  # 7. Default Max. CPC
    out += sep                  # 8. Keyword
    out += sep                  # 9. Keyword Type
    out += sep + headline       # 10. Headline
    out += sep + line1          # 11. Description Line 1
    out += sep + line2          # 12. Description Line 2
    out += sep + displayUrl     # 13. Display URL
    out += sep + destinationUrl # 14. Destination URL
    out += sep + 'Active'       # 15. Campaign Status
    out += sep + 'Active'       # 16. Ad Group Status
    out += sep + 'Active'       # 17. Ad Status
    return out + eol

#
# Return the formatted ad keyword definition
#
def getAdKeyword(campaignName, adGroup, maxCPC, keyword, keywordType):
    out = campaignName       # 0. Campaign Name
    out += sep               # 1. Campaign Daily Budget
    out += sep               # 2. Language
    out += sep               # 3. Geo Targeting
    out += sep               # 4. Proximity Targets
    out += sep               # 5.Ad Schedule
    out += sep + adGroup     # 6. Ad Group
    out += sep + maxCPC      # 7. Default Max. CPC
    out += sep + keyword     # 8.Keyword
    out += sep + keywordType # 9. Keyword Type
    out += sep               # 10. Headline
    out += sep               # 11. Description Line 1
    out += sep               # 12. Description Line 2
    out += sep               # 13. Display URL
    out += sep               # 14. Destination URL
    out += sep + 'Active'    # 15. Campaign Status
    out += sep + 'Active'    # 16. Ad Group Status
    out += sep               # 17. Ad Status
    out += sep + 'Active'    # 18. Keyword Status
    return out + eol
