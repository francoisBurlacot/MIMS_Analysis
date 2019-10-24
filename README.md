# MIMS Analysis
The purpose of this software is to treat and analyze data of a Membrane Inlet Mass Spectrometer (MIMS), in real time.


## Getting Started
To run the software (MIMS_Analysis.jar file), you need Java version 1.8.0_201 (or higher). See (https://www.java.com/fr/download/).

If your system doesn't support java 1.8.0, you can get the code in the software folder and compile it with your own java version (and create and executable with eclipse for example). If you want to do so, here is the external libraries used : common-collection-4.4.4, common-compress-1.18, jcommon-1.0.23, jfreechart-1.0.19, poi-4.1.0, xmlbeans-3.1.0 .

To launch the software you just have to double click on the MIMS_Analysis.jar element (or compile the code).



## Factor File
First, you need a factor file in csv. You can find an example in the repository.

Each row are values associated with various M/Z values (first column of factor file).

Isotope is the name of the associated molecule, as it will be displayed in the software (you can edit them).

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
Even if you don't want the software to display Ci, you need to enter a pH value by default (for example, choose a neutral pH: 7).  
Then you will be able to choose to dipslay or not Ci in the "display" menu of the software.

Denoised is set to be the M/Z values used for denoised calculus. If you don't want denoised calculus you can leave this cell empty. 
If you put a certain M/Z value for denoised calculus but the MIMS doesn't measure this M/Z, denoised calculus will not be enabled.

Every M/Z value measured by the MIMS should have a corresponding row in the factor file (except M/Z=12 and M/Z=27). 

Once you have loaded the factor file thanks to the file menu, you can load your data file (file of the MIMS).



## Data File
You can find an example of DataFile in the repository.

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
In this case you can choose a first molecule (gas1) and a second one (gas2) with drop down menus and when pressing the button, it will display the densoised gas exchange rate of (gas2) as a function of the gas concentration of (gas1).



### Curve Panel
On each curves panel, you have a "save data" button in the "File" menu at the left upper corner of the panel.  
It will save the data of the corresponding panel into an XLSX file (or XLS file if you choose an existing XLS file).  
To do so, be sure that the save file (if it exist) isn't open or in state "read only".  

In each curve panel, you can enter in fields a "Value for abscissa", a "Lower Bound for Average" and an "Upper Bound for Average".   
Once you have entered the desired values, you can press the calculate button, and the Table bellows will be updated with the corresponding values.  
The Min and Max columns are updated automatically.   



### Edit Menu
In the edit menu you can choose to play or pause the real time calculations. The software will still be working but new data will not be added to the dataset.   
Once you press play, every pending data are added at the same time in the dataset.  

The normalization factor button is set to modify the value of the normalization factor (set to one at the beginning).  
Every data that uses gas exchagne rates (like gas exchange rates or cumulated gas exchanges) are divided by this factor.  
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
