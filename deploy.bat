@call cd ..

@rem Dowloading dependencies
@if not exist utils (
    @call git clone --branch 2.0-SNAPSHOT --single-branch https://github.com/Pierre-Emmanuel41/utils
	
	@call cd utils
) else ( 
	@call cd utils
	
	@rem Pulling latest changes
	@call git pull
)

@echo Building project utils

@rem Building dependencies
@call mvn clean package install

@call cd ../communication

@echo Building project communication

@rem Building project
@call mvn clean package install