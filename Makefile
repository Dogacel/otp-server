

all:
	cd otp-web && npm run build
	rm -rf app/src/main/resources/web
	cp -r otp-web/out app/src/main/resources/web
	./gradlew shadowJar
	docker buildx build --platform linux/amd64 --push -t main-server:32000/otp-server .


run:
	./gradlew shadowJar
	docker buildx build -t otp-server .
	docker run -it --rm otp-server
