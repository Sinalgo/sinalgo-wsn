generateDataFrame <- function(mold, m1, m0.95, mneg1, key){
    matrixOld=as.matrix(mold[key])
    matrixM1=as.matrix(m1[key])
    matrixM0.95=as.matrix(m0.95[key])
    matrixMneg1=as.matrix(mneg1[key])
    indexOld=as.matrix(mold['Index'])
    indexM1=as.matrix(m1['Index'])
    indexM0.95=as.matrix(m0.95['Index'])
    indexMneg1=as.matrix(mneg1['Index'])
    sizeMold=length(matrixOld)
    sizeM1=length(matrixM1)
    sizeM09.5=length(matrixM0.95)
    sizeMneg1=length(matrixMneg1)
    groups=c(rep("ad-hoc", sizeMold), rep("p1.0", sizeM1), rep("p0.95", sizeM09.5), rep("p0.00", sizeMneg1))
    return(data.frame(series=groups, index=c(indexOld, indexM1, indexM0.95, indexMneg1), data=c(matrixOld, matrixM1, matrixM0.95, matrixMneg1)))
}

generateDataFrameNew <- function(m1, m0.95, mneg1, key){
    matrixM1=as.matrix(m1[key])
    matrixM0.95=as.matrix(m0.95[key])
    matrixMneg1=as.matrix(mneg1[key])
    indexM1=as.matrix(m1['Index'])
    indexM0.95=as.matrix(m0.95['Index'])
    indexMneg1=as.matrix(mneg1['Index'])
    sizeM1=length(matrixM1)
    sizeM09.5=length(matrixM0.95)
    sizeMneg1=length(matrixMneg1)
    groups=c(rep("p1.0", sizeM1), rep("p0.95", sizeM09.5), rep("p0.00", sizeMneg1))
    return(data.frame(series=groups, index=c(indexM1, indexM0.95, indexMneg1), data=c(matrixM1, matrixM0.95, matrixMneg1)))
}

library(ggplot2)

ggplot(data=generateDataFrame(sim_36_old, sim_36_1, sim_36_0.95, sim_36_neg1, "Real.Coverage"), aes(x=index, y=data, group=series)) + scale_color_brewer(palette="Dark2") + geom_line(aes(color=series)) + theme_classic() + labs(x="Tempo de Vida da Rede (u.t.)", y = "Cobertura (%)", color="Série")
ggplot(data=generateDataFrameNew(sim_36_1, sim_36_0.95, sim_36_neg1, "Sink.Coverage"), aes(x=index, y=data, group=series)) + scale_color_brewer(palette="Dark2") + geom_line(aes(color=series)) + theme_classic() + labs(x="Tempo de Vida da Rede (u.t.)", y = "Cobertura (%)", color="Série")
ggplot(data=generateDataFrameNew(sim_36_1, sim_36_0.95, sim_36_neg1, "Coverage.Delta...."), aes(x=index, y=data, group=series)) + scale_color_brewer(palette="Dark2") + geom_line(aes(color=series)) + theme_classic() + labs(x="Tempo de Vida da Rede (u.t.)", y = paste("\u0394", " Cobertura (%)"), color="Série")
ggplot(data=generateDataFrame(sim_36_old, sim_36_1, sim_36_0.95, sim_36_neg1, "Residual.Energy"), aes(x=index, y=data, group=series)) + scale_color_brewer(palette="Dark2") + geom_line(aes(color=series)) + theme_classic() + labs(x="Tempo de Vida da Rede (u.t.)", y = "Energia (mAh)", color="Série")

ggplot(data=generateDataFrame(sim_49_old, sim_49_1, sim_49_0.95, sim_49_neg1, "Real.Coverage"), aes(x=index, y=data, group=series)) + scale_color_brewer(palette="Dark2") + geom_line(aes(color=series)) + theme_classic() + labs(x="Tempo de Vida da Rede (u.t.)", y = "Cobertura (%)", color="Série")
ggplot(data=generateDataFrameNew(sim_49_1, sim_49_0.95, sim_49_neg1, "Sink.Coverage"), aes(x=index, y=data, group=series)) + scale_color_brewer(palette="Dark2") + geom_line(aes(color=series)) + theme_classic() + labs(x="Tempo de Vida da Rede (u.t.)", y = "Cobertura (%)", color="Série")
ggplot(data=generateDataFrameNew(sim_49_1, sim_49_0.95, sim_49_neg1, "Coverage.Delta...."), aes(x=index, y=data, group=series)) + scale_color_brewer(palette="Dark2") + geom_line(aes(color=series)) + theme_classic() + labs(x="Tempo de Vida da Rede (u.t.)", y = paste("\u0394", " Cobertura (%)"), color="Série")
ggplot(data=generateDataFrame(sim_49_old, sim_49_1, sim_49_0.95, sim_49_neg1, "Residual.Energy"), aes(x=index, y=data, group=series)) + scale_color_brewer(palette="Dark2") + geom_line(aes(color=series)) + theme_classic() + labs(x="Tempo de Vida da Rede (u.t.)", y = "Energia (mAh)", color="Série")

ggplot(data=generateDataFrame(sim_64_old, sim_64_1, sim_64_0.95, sim_64_neg1, "Real.Coverage"), aes(x=index, y=data, group=series)) + scale_color_brewer(palette="Dark2") + geom_line(aes(color=series)) + theme_classic() + labs(x="Tempo de Vida da Rede (u.t.)", y = "Cobertura (%)", color="Série")
ggplot(data=generateDataFrameNew(sim_64_1, sim_64_0.95, sim_64_neg1, "Sink.Coverage"), aes(x=index, y=data, group=series)) + scale_color_brewer(palette="Dark2") + geom_line(aes(color=series)) + theme_classic() + labs(x="Tempo de Vida da Rede (u.t.)", y = "Cobertura (%)", color="Série")
ggplot(data=generateDataFrameNew(sim_64_1, sim_64_0.95, sim_64_neg1, "Coverage.Delta...."), aes(x=index, y=data, group=series)) + scale_color_brewer(palette="Dark2") + geom_line(aes(color=series)) + theme_classic() + labs(x="Tempo de Vida da Rede (u.t.)", y = paste("\u0394", " Cobertura (%)"), color="Série")
ggplot(data=generateDataFrame(sim_64_old, sim_64_1, sim_64_0.95, sim_64_neg1, "Residual.Energy"), aes(x=index, y=data, group=series)) + scale_color_brewer(palette="Dark2") + geom_line(aes(color=series)) + theme_classic() + labs(x="Tempo de Vida da Rede (u.t.)", y = "Energia (mAh)", color="Série")

ggplot(data=generateDataFrame(sim_81_old, sim_81_1, sim_81_0.95, sim_81_neg1, "Real.Coverage"), aes(x=index, y=data, group=series)) + scale_color_brewer(palette="Dark2") + geom_line(aes(color=series)) + theme_classic() + labs(x="Tempo de Vida da Rede (u.t.)", y = "Cobertura (%)", color="Série")
ggplot(data=generateDataFrameNew(sim_81_1, sim_81_0.95, sim_81_neg1, "Sink.Coverage"), aes(x=index, y=data, group=series)) + scale_color_brewer(palette="Dark2") + geom_line(aes(color=series)) + theme_classic() + labs(x="Tempo de Vida da Rede (u.t.)", y = "Cobertura (%)", color="Série")
ggplot(data=generateDataFrameNew(sim_81_1, sim_81_0.95, sim_81_neg1, "Coverage.Delta...."), aes(x=index, y=data, group=series)) + scale_color_brewer(palette="Dark2") + geom_line(aes(color=series)) + theme_classic() + labs(x="Tempo de Vida da Rede (u.t.)", y = paste("\u0394", " Cobertura (%)"), color="Série")
ggplot(data=generateDataFrame(sim_81_old, sim_81_1, sim_81_0.95, sim_81_neg1, "Residual.Energy"), aes(x=index, y=data, group=series)) + scale_color_brewer(palette="Dark2") + geom_line(aes(color=series)) + theme_classic() + labs(x="Tempo de Vida da Rede (u.t.)", y = "Energia (mAh)", color="Série")

ggplot(data=generateDataFrame(sim_100_old, sim_100_1, sim_100_0.95, sim_100_neg1, "Real.Coverage"), aes(x=index, y=data, group=series)) + scale_color_brewer(palette="Dark2") + geom_line(aes(color=series)) + theme_classic() + labs(x="Tempo de Vida da Rede (u.t.)", y = "Cobertura (%)", color="Série")
ggplot(data=generateDataFrameNew(sim_100_1, sim_100_0.95, sim_100_neg1, "Sink.Coverage"), aes(x=index, y=data, group=series)) + scale_color_brewer(palette="Dark2") + geom_line(aes(color=series)) + theme_classic() + labs(x="Tempo de Vida da Rede (u.t.)", y = "Cobertura (%)", color="Série")
ggplot(data=generateDataFrameNew(sim_100_1, sim_100_0.95, sim_100_neg1, "Coverage.Delta...."), aes(x=index, y=data, group=series)) + scale_color_brewer(palette="Dark2") + geom_line(aes(color=series)) + theme_classic() + labs(x="Tempo de Vida da Rede (u.t.)", y = paste("\u0394", " Cobertura (%)"), color="Série")
ggplot(data=generateDataFrame(sim_100_old, sim_100_1, sim_100_0.95, sim_100_neg1, "Residual.Energy"), aes(x=index, y=data, group=series)) + scale_color_brewer(palette="Dark2") + geom_line(aes(color=series)) + theme_classic() + labs(x="Tempo de Vida da Rede (u.t.)", y = "Energia (mAh)", color="Série")
