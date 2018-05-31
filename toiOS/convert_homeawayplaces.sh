#!/bin/bash
################################################################################
#															                   #
#(the comment line above ensures this will run under bash) 	                   #
# Java to Objective-C conversion script						                   #
#															                   #
# WARNING : VARIABLE NAMES SHOULD BE PRESERVED AS IT IS. 	                   #
#															                   #
################################################################################

current_directory="$PWD"
project_directory="$(dirname "$current_directory")"
path_to_jars="$project_directory/places-api/libs"

j2objc_tool_path="/Volumes/MyData/Development/jVanila-iOS-Framework/jVanilaScript-j2objc"
android_java_code_path="$j2objc_tool_path/tempsource"
objc_code_destination_folder="ConvertedFiles/RefCount"
classpath_to_jars=".:$path_to_jars/jvanila-core-v0.0.1.jar:$path_to_jars/jvanila-mobile-v0.0.1.jar"

local_ios_project_name="/Volumes/MyData/Development/xCodeWS/HomeAwayPlacesIOS"
ios_working_copy_path="$local_ios_project_name/HomeAwayPlacesIOS/Source/CodeConversion"

android_working_copy_path1="$project_directory/app/src/main/java"
android_working_copy_path2="$project_directory/places-foursquare/src/main/java"
android_working_copy_path3="$project_directory/places-api/src/main/java"

android_path_to_R_file="$project_directory/app/build/generated/source/r/debug/com/homeaway/homeawayplaces/droid/R.java"
app_R_file_import="import com.homeaway.homeawayplaces.droid.R;"
module_R_file_imports=(
"import com.homeaway.homeawayplaces.foursquare.droid.R;"
"import com.homeaway.homeawayplaces.domain.droid.R;"
)

#Load desired folders to convert
path_to_prefixes_file="$current_directory/prefixes_homeawayplaces.properties"
source_files_path_array=(
"com/homeaway/homeawayplaces/droid"
"com/homeaway/homeawayplaces/views"
"com/homeaway/homeawayplaces/presenters"
"com/homeaway/homeawayplaces/binders"
"com/homeaway/homeawayplaces/locale"
"com/homeaway/homeawayplaces/sync/dtos"
"com/homeaway/homeawayplaces/sync/gateways"
"com/homeaway/homeawayplaces/domain"
"com/homeaway/homeawayplaces/domain/sync"
"com/homeaway/homeawayplaces/domain/sync/daos"
"com/homeaway/homeawayplaces/domain/sync/dtos"
"com/homeaway/homeawayplaces/foursquare"
"com/homeaway/homeawayplaces/foursquare/sync"
"com/homeaway/homeawayplaces/foursquare/sync/dtos"
"com/homeaway/homeawayplaces/foursquare/sync/daos"
"com/homeaway/homeawayplaces/foursquare/sync/gateways"
)

android_working_copy_paths=(
$android_working_copy_path1 
$android_working_copy_path2
$android_working_copy_path3
)

# Include api methods
source $j2objc_tool_path/src_scripts/api_methods.sh
# Start conversion
api_start_conversion