import { Injectable } from '@angular/core';
import * as faceapi from 'face-api.js';

@Injectable({ providedIn: 'root' })
export class ReconhecimentoFacialService {

  private modelosCarregados = false;

  async carregarModelos(): Promise<void> {
    if (this.modelosCarregados) return;
    const url = '/models';
    await Promise.all([
      faceapi.nets.tinyFaceDetector.loadFromUri(url),
      faceapi.nets.faceLandmark68Net.loadFromUri(url),
      faceapi.nets.faceRecognitionNet.loadFromUri(url)
    ]);
    this.modelosCarregados = true;
    console.log('✅ Modelos face-api.js carregados');
  }

 async obterDescriptor(imagemBase64: string): Promise<Float32Array | null> {
  try {
    console.log('📷 Tamanho da foto:', imagemBase64?.length, 'Início:', imagemBase64?.substring(0, 30));
    const img = await this.base64ParaImagem(imagemBase64);
    console.log('📷 Imagem carregada:', img.width, 'x', img.height);
    const deteccao = await faceapi
      .detectSingleFace(img, new faceapi.TinyFaceDetectorOptions({
        inputSize: 320,
        scoreThreshold: 0.3
      }))
      .withFaceLandmarks()
      .withFaceDescriptor();
    console.log('📷 Detecção na foto:', deteccao ? 'ENCONTROU' : 'NÃO ENCONTROU');
    return deteccao ? deteccao.descriptor : null;
  } catch (e) {
    console.error('Erro ao obter descriptor:', e);
    return null;
  }
}

 async obterDescriptorDeVideo(video: HTMLVideoElement): Promise<Float32Array | null> {
  try {
   const deteccao = await faceapi
  .detectSingleFace(video, new faceapi.TinyFaceDetectorOptions({
    inputSize: 320,
    scoreThreshold: 0.3
  }))
  .withFaceLandmarks()
  .withFaceDescriptor();
    
    console.log('🔍 Detecção:', deteccao ? 'ROSTO ENCONTRADO' : 'SEM ROSTO');
    return deteccao ? deteccao.descriptor : null;
  } catch (e) {
    console.error('Erro ao obter descriptor:', e);
    return null;
  }
}

  calcularSimilaridade(desc1: Float32Array, desc2: Float32Array): number {
    const distancia = faceapi.euclideanDistance(desc1, desc2);
    return 1 - distancia;
  }

 async reconhecerNaLista(
  descriptorAtual: Float32Array,
  funcionarios: { id: number; nome: string; fotoBase64: string }[]
): Promise<{ funcionario: any; confianca: number } | null> {

  let melhorMatch: any = null;
  let melhorConfianca = 0;
  const LIMIAR = 0.55;

  console.log(`🔍 Comparando com ${funcionarios.length} funcionário(s)...`);

  for (const func of funcionarios) {
    const descriptor = await this.obterDescriptor(func.fotoBase64);
    if (!descriptor) {
      console.log(`⚠️ ${func.nome}: sem descriptor`);
      continue;
    }

    const similaridade = this.calcularSimilaridade(descriptorAtual, descriptor);
    console.log(`👤 ${func.nome}: ${(similaridade * 100).toFixed(1)}% (limiar: ${LIMIAR * 100}%)`);

    if (similaridade > melhorConfianca && similaridade > LIMIAR) {
      melhorConfianca = similaridade;
      melhorMatch = func;
    }
  }

  console.log(melhorMatch ? `✅ Match: ${melhorMatch.nome} (${(melhorConfianca * 100).toFixed(0)}%)` : '❌ Nenhum match');
  return melhorMatch ? { funcionario: melhorMatch, confianca: melhorConfianca } : null;
}

  private base64ParaImagem(base64: string): Promise<HTMLImageElement> {
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.onload = () => resolve(img);
      img.onerror = reject;
      img.src = base64.startsWith('data:') ? base64 : `data:image/jpeg;base64,${base64}`;
    });
  }
}