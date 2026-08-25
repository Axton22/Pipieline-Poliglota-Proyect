program etapa2_metricas
    implicit none
    character(len=100) :: encabezado
    character(len=20)  :: id, estacion
    real :: temperatura, precipitacion, viento, bateria
    real :: suma_temp, suma_viento, suma_bateria, precip_acum
    real :: temp_max, temp_min, viento_max
    integer :: n, ios

    open(unit=10, file="../datos/datos_normalizados.csv", status="old", action="read")
    open(unit=20, file="../datos/metricas.csv", status="replace", action="write")

    read(10, '(A)') encabezado   ! saltar la cabecera del CSV

    n = 0
    suma_temp    = 0.0
    suma_viento  = 0.0
    suma_bateria = 0.0
    precip_acum  = 0.0
    temp_max     = -1.0e30
    temp_min     =  1.0e30
    viento_max   = -1.0e30

    do
        read(10, *, iostat=ios) id, estacion, temperatura, precipitacion, viento, bateria
        if (ios /= 0) exit   ! fin de archivo

        n = n + 1
        suma_temp    = suma_temp + temperatura
        suma_viento  = suma_viento + viento
        suma_bateria = suma_bateria + bateria
        precip_acum  = precip_acum + precipitacion

        if (temperatura > temp_max) temp_max = temperatura
        if (temperatura < temp_min) temp_min = temperatura
        if (viento > viento_max)    viento_max = viento
    end do

    close(10)

    write(20, '(A)') "METRICA,VALOR"
    if (n > 0) then
        write(20, '(A,F0.2)') "TEMPERATURA_PROMEDIO,",   suma_temp / n
        write(20, '(A,F0.2)') "TEMPERATURA_MAXIMA,",     temp_max
        write(20, '(A,F0.2)') "TEMPERATURA_MINIMA,",     temp_min
        write(20, '(A,F0.2)') "PRECIPITACION_ACUMULADA,", precip_acum
        write(20, '(A,F0.2)') "VIENTO_PROMEDIO,",        suma_viento / n
        write(20, '(A,F0.2)') "VIENTO_MAXIMO,",          viento_max
        write(20, '(A,F0.2)') "BATERIA_PROMEDIO,",       suma_bateria / n
    else
        write(20, '(A)') "SIN_DATOS,0"
    end if

    close(20)

    print *, "Registros procesados: ", n
    print *, "metricas.csv generado correctamente"

end program etapa2_metricas