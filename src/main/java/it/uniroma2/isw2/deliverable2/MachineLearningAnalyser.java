package it.uniroma2.isw2.deliverable2;

import it.uniroma2.isw2.deliverable2.entities.AnalysisProfile;
import it.uniroma2.isw2.deliverable2.entities.AnalysisRun;
import it.uniroma2.isw2.deliverable2.entities.EvaluationResults;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MachineLearningAnalyser {

	public static final int VERSION_IDX = 0;
	public static final int NAME_IDX = 1;
	private static final Logger LOGGER = Logger.getLogger("ISW2-DELIVERABLE-2(ML)");

	private String projectName;
	private String resultsFolder;
	private Instances fullDataset;
	private List<String> versionNames;

	public MachineLearningAnalyser(String projectName, String resultsFolder) throws Exception {
		this.projectName = projectName;
		this.resultsFolder = resultsFolder;
		this.fullDataset = new DataSource(String.format("%s%s_all_metrics.arff", this.resultsFolder, this.projectName))
				.getDataSet();
		this.versionNames = getVersionNames(this.fullDataset);
	}

	public void finalizeAnalysis() throws Exception {

		File csvDataset = new File(String.format("%s%s_final_results.csv", this.resultsFolder, this.projectName));
		try (FileWriter writer = new FileWriter(csvDataset, false)) {
			writer.append(EvaluationResults.CSV_HEADER);
			for (AnalysisProfile profile : AnalysisProfile.generateAllProfiles())
				this.walkForward(profile, writer);
		}
	}

	private void walkForward(AnalysisProfile profile, FileWriter writer) throws Exception {

		for (int testingIdx = 1; testingIdx < this.versionNames.size(); ++testingIdx) {
			AnalysisRun actualRun = new AnalysisRun(fullDataset, profile, projectName);

			for (Instance row : fullDataset) {
				if (versionNames.indexOf(row.stringValue(VERSION_IDX)) == testingIdx)
					actualRun.addToTesting(row);

				else if (versionNames.indexOf(row.stringValue(VERSION_IDX)) < testingIdx)
					actualRun.addToTraining(row);
			}

			actualRun.removeUnwantedAttributes();

			actualRun.setupClassIndexes();
			actualRun.initializeResults(this.fullDataset.size());
			actualRun.evaluate(profile);

			LOGGER.log(Level.INFO, "New result added: {0}", actualRun.getResults());
			writer.append(String.format("%s%n", actualRun.getResults()));
		}
	}

	private List<String> getVersionNames(Instances fullDataset) {
		List<String> versions = new ArrayList<>();

		for (Instance row : fullDataset)
			if (!versions.contains(row.stringValue(VERSION_IDX)))
				versions.add(row.stringValue(VERSION_IDX));

		return versions;
	}
}
