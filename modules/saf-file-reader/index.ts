
// Import the native module. On web, it will be resolved to SafFileReader.web.ts
// and on native platforms to SafFileReader.ts
import SafFileReaderModule from './src/SafFileReaderModule';

export async function getFile() {
  return await SafFileReaderModule.getFile();
}
