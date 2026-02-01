package br.com.spring_react.blog.infra.services;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class MultiPartService {

    @Value("${api.file.upload.path}")
    private String uploadPath;

    private final List<String> ALLOWED_MIMES = List.of("image/jpeg", "image/png", "image/webp");

    public String processImage(MultipartFile file, String folder, int targetWidth) {
        try {
            // validação de mimetype
            if (!ALLOWED_MIMES.contains(file.getContentType())) {
                throw new BadRequestException("Invalid file type. Use JPG, PNG or WebP.");
            }

            // gera um nome único já com a extensão .webp
            String fileName = UUID.randomUUID().toString() + ".webp";
            Path directoryPath = Paths.get(uploadPath, folder); // define o caminho da imagem

            // se o diretório de imagens (uploads) não existir, cria-o
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            // carrega e processa o arquivo de imagem a partir da stream do arquivo
            ImmutableImage image = ImmutableImage.loader().fromStream(file.getInputStream());

            // redimensiona e converte para .webp com o WebpWriter
            image.scaleToWidth(targetWidth)
                    .output(WebpWriter.DEFAULT, directoryPath.resolve(fileName));

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not process and save the image file.");
        }
    }

    public void deleteImage(String folder, String filename) {
        try {
            Path filePath = Paths.get(uploadPath, folder, filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // vazio pra evitar erros se o arquivo tiver sumido
        }
    }
}
