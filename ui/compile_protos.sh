PROTOC_PATH="./node_modules/protoc/protoc/bin/protoc"
PROTOC_GEN_TS_PATH="./node_modules/.bin/protoc-gen-ts"

# Directory to write generated code to (.js and .d.ts files) 
OUT_DIR="./src/app/models"

mkdir -p src/app/models

${PROTOC_PATH} \
    --plugin="protoc-gen-ts=${PROTOC_GEN_TS_PATH}" \
    --js_out="import_style=commonjs,binary:${OUT_DIR}" \
    --ts_out="${OUT_DIR}" \
    --proto_path="../protos" \
    ../protos/*
