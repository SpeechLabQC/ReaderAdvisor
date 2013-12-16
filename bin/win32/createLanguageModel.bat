::Eliminate the echo to the user
@echo off
::Set Local Run
SETLOCAL enabledelayedexpansion enableextensions
:: Save the name of the Language Model
set "file=%1"
set "path=%2"
:: Only create the files if the User provides a valid argument name
if NOT "%1" == "" (
    if NOT "%2" == "" (
        :: Check if the file exists
        if exist "!file!.txt" (
            :: Creating the vocabulary for the file
            !path!\text2wfreq < !file!.txt | !path!\wfreq2vocab > "!file!.vocab"
            :: Check that the vocabulary file was created successfully
            if exist "!file!.vocab" (
                :: Generate the arpa format language models
                !path!\text2idngram -vocab "!file!.vocab" -idngram "!file!.idngram" < "!file!.txt"
                :: Check that the idngram file exists
                if exist "!file!.idngram" (
                    :: Create the Arpa Language Model
                    start /WAIT !path!\idngram2lm -vocab_type 0 -idngram "!file!.idngram" -vocab "!file!.vocab" -arpa "!file!.lm"
                    :: Delete the .idngram and .vocab files
                    DEL "!file!.idngram"
                    DEL "!file!.vocab"
                    :: Check that the Arpa Language Model was created successfully
                    if exist "!file!.lm" (
                        :: Create the Binary Arpa Language Model
                        start /WAIT !path!\sphinx_lm_convert -i "!file!.lm" -o "!file!.DMP"
                        exit /B 1
                    ) else (
                        echo "!file!.lm does not exists."
                    )
                    ::Skip else
                    GOTO END
                ) else (
                    echo "!file!.idngram does not exists."
                )
                ::Skip else
                GOTO END
            ) else (
                echo "!file!.vocab does not exists."
            )
            ::Skip else
            GOTO END
        ) else (
            echo "!file!.txt does not exists. Try again when the file is created."
        )
	    :: Skip else
	    GOTO END
    ) else (
        echo "Usage : createLanguageModel.batch <language_model_name> <path_to_executable_files>"
    )
) else (
    echo "Usage : createLanguageModel.batch <language_model_name> <path_to_executable_files>"
)
:: END
:END
::End Local Run
ENDLOCAL
exit /B -1
