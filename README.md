# MIMS Analysis
The purpose of this software is to treat and analyze data of a Membrane Inlet Mass Spectrometer (MIMS), in real time.


## Getting Started
To run the software (java .jar file), you need Java version 1.8.0_201 (or higher). See (https://www.java.com/fr/download/).
If your system doesn't support java 1.8.0, you can get the code in the source code folder and compile it with your own java version (and create and executable with eclipse for example).
To launch the software you just have to double click on the MIMS_Analysis.jar element (or compile the code).



## Factor File
First, you need a factor file in csv. Here is an exemple:

M/Z;Isotope;Concentration;Signal;Baseline;Consumption;Ratio;pH;Denoised
2;H2;7.83E+02;1.09E-11;5.14E-14;0.2;0;7;28
3;HD;8.26E+02;3.86E-11;5.00E-15;2.04E-01;0;;
4;D2;8.94E+02;4.89E-11;4.00E-14;1.90E-01;0;;
18;H2O;1;25;1;1;0;;
28;N2;5.00E+02;5.85E-11;2.12E-12;3.36E-02;0;;
30;NO;1.76E+03;2.03E-10;7.00E-15;4.09E-02;0.311;;
31;EtOH;1;23;1;1;0.2241;;
31;MetOH;1;3;1;1;0.2241;;
32;O2;2.65E+02;3.65E-11;9.84E-13;6.84E-02;0;;
34;H2S;1;10;1;1;0;;
36;18O2;2.65E+02;2.62E-11;0;6.51E-02;0;;
40;Ar;1.20E+01;1.61E-12;3.00E-14;3.98E-02;0;;
44;CO2;1.07E+01;5.12E-13;3.90E-14;0;0.0871;;
44;N2O;2.50E+04;1.06E-09;3.90E-14;0.0140477;0.0871;;
45;13CO2;1.29E+01;4.03E-13;1.00E-15;0.00E+00;0;;
47;13C18OO;1.29E+01;4.03E-13;1.00E-15;0.00E+00;0;;
49;13C18O2;1.29E+01;4.03E-13;1.00E-15;0.00E+00;0;;

Each row are values associated with various M/Z values (first column of factor file). 
Isotope is the name of the associated Molecule, as it will be displayed in the software (you can edit them).
Concentration, signal, baseline and consumption are supposed to be changed each calibration.
Ratio are constants values for NO, EtOH, METOH, CO2, N2O concentration calculus:
A_(NO) (30)=A_(NO)(12)-A(30)/0.311
A_(N_2 O) (44)=A(44)-(A_(CO_2 )(12))/0.0871
A_(ETOH) (31)=A(31)-(A_((METOH)) (27))/0.0871
A_(ETOH) (31)=(A_(ETOH)(27))/0.2241
A_(METOH)(31)=A(27)-(A_((METOH))(27))//0.2241
You can edit them if you want.
You can change the title of each row, as it better suits you.

### Common Bugs
The pH value and Denoised value are associated with the M/Z=2 row. You must not erase this row, even if you don't measure M/Z=2. 
Even if you don't want the software to display Ci, you need to enter a pH value by default (just put 1). 
Then you will be able to choose to dipslay or not Ci in the "display" menu of the software.

Denoised is set to be the M/Z values used for denoised calculus. If you don't want denoised calculus you can leave this cell empty. 
If you put a certain M/Z value for denoised calculus but the MIMS don't measure this M/Z, denoised calculus will not be enabled.

Every M/Z value measured by the MIMS should have a corresponding row in the factor file (except M/Z=12 and M/Z=27). 

Once you have loaded the factor file thanks to the file menu, you can load your data file (file of the MIMS).



## Data File
your data file should look like this:

Time&Date;$Flags$;18;28;32;36;44;                                                                               
2019/07/12 16:14:55.74;;5.28E-11;3.9035E-11;2.125E-11;2.0535E-11;1.669E-12;
2019/07/12 16:14:59.01;;5.2767E-11;3.8926E-11;2.1164E-11;2.0448E-11;1.7437E-12;
2019/07/12 16:15:02.34;;5.2762E-11;3.8859E-11;2.1105E-11;2.0381E-11;1.8025E-12;
2019/07/12 16:15:05.67;;5.2766E-11;3.882E-11;2.1053E-11;2.0335E-11;1.856E-12;
2019/07/12 16:15:09.00;;5.2751E-11;3.884E-11;2.1055E-11;2.0324E-11;1.8959E-12;
2019/07/12 16:15:12.28;;5.2766E-11;3.8906E-11;2.1056E-11;2.033E-11;1.9368E-12;
2019/07/12 16:15:15.58;;5.2746E-11;3.8946E-11;2.1034E-11;2.0292E-11;1.9658E-12;
2019/07/12 16:15:25.68;;5.3002E-11;3.8754E-11;2.0732E-11;1.9957E-11;2.0098E-12;
2019/07/12 16:15:28.62;;5.2849E-11;3.8556E-11;2.0623E-11;1.9852E-11;2.0285E-12;
2019/07/12 16:15:31.89;;5.2796E-11;3.8405E-11;2.0493E-11;1.9734E-11;2.037E-12;
2019/07/12 16:15:35.14;;5.2761E-11;3.8203E-11;2.0356E-11;1.959E-11;2.0458E-12;
2019/07/12 16:15:38.45;;5.2754E-11;3.806E-11;2.0254E-11;1.9494E-11;2.0518E-12;
2019/07/12 16:15:41.74;;5.2686E-11;3.7924E-11;2.0172E-11;1.9401E-11;2.0573E-12;
2019/07/12 16:15:45.05;;5.2711E-11;3.7871E-11;2.0136E-11;1.9358E-11;2.0635E-12;
2019/07/12 16:15:48.33;;5.2697E-11;3.7884E-11;2.0119E-11;1.9347E-11;2.0716E-12;
2019/07/12 16:15:51.63;;5.2697E-11;3.7842E-11;2.0061E-11;1.9283E-11;2.0765E-12;
2019/07/12 16:15:54.90;;5.2705E-11;3.7781E-11;1.9972E-11;1.9203E-11;2.078E-12;
2019/07/12 16:15:58.18;;5.2733E-11;3.7682E-11;1.9874E-11;1.9099E-11;2.0799E-12;
2019/07/12 16:16:01.47;;5.2693E-11;3.7575E-11;1.9804E-11;1.9013E-11;2.0846E-12;
2019/07/12 16:16:04.72;;5.2676E-11;3.748E-11;1.9721E-11;1.8938E-11;2.0804E-12;
2019/07/12 16:16:07.98;;5.2679E-11;3.7367E-11;1.9602E-11;1.883E-11;2.0846E-12;
2019/07/12 16:16:11.30;;5.2577E-11;3.7203E-11;1.9493E-11;1.8712E-11;2.0838E-12;

It's really important that each row ends with a ";" and that the second row, named "$Flags$" is empty. 
If it's not the case change the parameters of your MIMS, or change the code (in the getData(path) function) to suit your own Data file.

Once you have loaded your Data File, you will be able to use the software as you want.



## Button Functionnality
### Step for sliding average
There is at most three "Step for sliding Average" average field displayed (depending of your experiment).
This field takes a number "step" and realized a sliding average on each point with "step" point before and "step" point after the current point.
To use correctly this field you need at least 2*step+1 points (or data row) in order to display something.
In another case, an error message will be displayed.

### Drop Down Menu 
A drop down menu associated with the V(gas2)=F(c(gas1)) button is displayed if you have denoised calculus.
In this case you can choose a first molecule (gas1) and a second one (gas2) with drop down menus and when pressing the button, it will display V(gas2)=F(c(gas1)).



### Curve Panel
On each curves panel, you have a "save data" button in the "File" menu at the left upper corner of the panel.
It will save the data of the corresponding panel into an XLSX file (or XLS file if you choose an existing XLS file).
To do so, be sure that the save file (if it exist) isn't open or in state "read only".

In each curve panel, you can enter in fields a "Value for abscissa", a "Lower Bound for Average" and an "Upper Bound for Average". 
Once you have entered the desired values, you can press the calculate button, and the Table bellows will be updated with the corresponding values.
The Min and Max columns are updated automatically. 



### Edit Menu
In the edit menu you can choose to play or pause the process. The software will still be working but new data will not be added to the dataset. 
Once you pressed play, every pending data are added at the same time in the dataset.

The normalization factor button is set to modify the value of the normalization factor (set to one at the beginning). 
Every data implicating rates (like gas exchange rates or cumulated gas exchanges) are divided by this factor.
Once you changed the normalization factor values, every chart panel will be closed and the dataset updated. You will have to reopen each wanted chart panel.



### Display Menu
In the display menu, you can choose to display H2O (18) or not and to calculate Ci or not.
Once you changed one of the parameters, every chart panel will be closed and the dataset updated. You will have to reopen each wanted chart panel.



### About Menu
You will find information regarding this software. (license and version information)


## General Information
If there is M/Z=44 and not M/Z=12, the software interprete M/Z=44 to be the first row in the factor file with M/z=44 (in our exemple CO2). 
It's the same for M/Z=31 if we don't have M/Z=27 (in our example EtOH).

Denoising M/Z can't be 12, 18 or 27.

Every displayed number are round with 3 significant digit. If you want more (or less) significant digit you can change the number 3 in significantDigit(number,3) in the source code.

The denoising M/Z used isn't displayed in the denoised charts (because it's just a horzontal straight line).



## Bug Issues
If you meet bugs during your experiments, you can use the MIMS_Analysis.log file to try to debug and understand the problem.



## Built With
Java 1.8.0_221



## Contributing
Feel free to modify and improve this version as long as it's in accord with the license of this software.



## Versioning
1.0.1



## Authors
Francois Burlacot, contacts: francois.burlacot@telecom-paris.fr; adrien.burlacot@polytechnique.edu



## License
Copyright (C) 2019 - F.Burlacot
This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program. If not, see: https://www.gnu.org/licenses/
