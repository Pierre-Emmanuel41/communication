@call cd ..

@rem Downloading dependencies
@if not exist utils (
	@echo Cloning git repo for project utils
    @call git clone https://github.com/Pierre-Emmanuel41/utils.git
	
	@call cd utils
) else ( 
	@call cd utils
	
	@echo Pulling latest changes for project utils
	@call git pull
)

@rem Building dependencies
@echo Building project utils
@call mvn clean package install

@echo Building project communication
@call cd ../communication
@call mvn clean package install