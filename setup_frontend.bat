@echo off
pushd \\wsl.localhost\Ubuntu\home\nrk\eclipse-workspace\accounts\account-web
call npm install
call npm install -D tailwindcss postcss autoprefixer
call npx tailwindcss init -p
call npm install lucide-react react-router-dom axios clsx tailwind-merge framer-motion input-otp
popd
